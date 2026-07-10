package org.litebridgedb.maven;

import com.github.javaparser.ast.CompilationUnit;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.jspecify.annotations.Nullable;
import org.litebridgedb.commons.ClassUtils;
import org.litebridgedb.commons.ObjectUtils;
import org.litebridgedb.db.spi.DatabaseProvider;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.TableMetaData;
import org.litebridgedb.maven.config.reverse.DatabaseConfig;
import org.litebridgedb.maven.config.reverse.RevEngInputConfig;
import org.litebridgedb.maven.config.reverse.RevEngOutputConfig;
import org.litebridgedb.maven.config.reverse.SqlTypeMappingConfig;
import org.litebridgedb.maven.config.reverse.TableMappingConfig;
import org.litebridgedb.maven.reverse.EntityGenerator;
import org.litebridgedb.maven.reverse.GeneratedEntity;
import org.litebridgedb.maven.reverse.ManyToManyMapper;
import org.litebridgedb.maven.reverse.ManyToManyMapping;
import org.litebridgedb.maven.util.JavaFileWriter;
import org.litebridgedb.maven.util.PackageInfoGenerator;
import org.litebridgedb.orm.persistence.TransactionalDatabaseProvider;
import org.litebridgedb.orm.tx.DefaultTransactionManager;
import org.litebridgedb.orm.tx.LitebridgeDriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A Maven Mojo for reverse engineering database tables into Java entity classes.
 * <p>
 * This Mojo generates Java source files based on the provided database schema, input
 * configuration, and output configuration settings. It performs table metadata extraction,
 * applies custom mappings (if configured), and writes the resulting entity classes to the
 * specified output directory.
 * <p>
 * Output can be fine-tuned to control the generated entity classes, such as specifying
 * the package name, class name, class modifiers, field types, nullability annotations, etc.
 * <p>
 * This Mojo is executed during the <i>generate-sources</i> phase of the Maven build lifecycle by default.
 */
@Mojo(name = "reverse-engineer", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public final class ReverseEngineerMojo extends AbstractMojo {

    /**
     * Maven project instance
     */
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    /**
     * Skips plugin execution if {@code true}
     */
    @Parameter(defaultValue = "false")
    private boolean skip;

    /**
     * Database configuration.
     */
    @Parameter(required = true)
    private DatabaseConfig database;

    /**
     * Input configuration.
     * <p>
     * Used to specify the tables to reverse engineer.
     */
    @Parameter(required = true)
    private RevEngInputConfig input;

    /**
     * SQL data type override configuration.
     * <p>
     * This is an optional global setting that allows specifying exact Java classes to use for SQL data types.
     */
    @Parameter
    private @Nullable List<SqlTypeMappingConfig> sqlTypeMappings;

    /**
     * Additional table mapping configuration.
     * <p>
     * This is optional, but allows setting up inter-table relationships, per-column data type overrides, etc.
     */
    @Parameter
    private @Nullable List<TableMappingConfig> tableMappings;

    /**
     * Generated output configuration.
     */
    @Parameter(required = true)
    private RevEngOutputConfig output;

    @Override
    public void execute() throws MojoExecutionException {
        if (skip) {
            getLog().debug("Skipping Litebridge reverse engineering because skip == true");
            return;
        }

        getLog().info("Reverse engineering Litebridge entities");
        validateConfig();

        if (getLog().isDebugEnabled()) {
            getLog().debug("Creating datasource for URL: " + database.getUrl());
        }

        final LitebridgeDriverManagerDataSource dataSource = new LitebridgeDriverManagerDataSource(database.getUrl(), database.getUser(), database.getPassword());
        final TransactionalDatabaseProvider databaseProvider = createDatabaseProvider(dataSource);
        final EntityGenerator entityGenerator = new EntityGenerator(sqlTypeMappings, tableMappings, output, getLog());
        final PackageInfoGenerator packageInfoGenerator = new PackageInfoGenerator(output);
        final JavaFileWriter javaFileWriter = new JavaFileWriter(project, output, getLog());

        // Get table metadata
        final Map<String, TableMetaData> tableMetaDataMap = new HashMap<>(input.getTables().size());

        for (final String tableName : input.getTables()) {
            final TableMetaData tableMetaData;

            try {
                tableMetaData = databaseProvider.tableMetaData(new Table(tableName), databaseProvider.transactionManager());
            } catch (SQLException ex) {
                throw new MojoExecutionException("Failed to retrieve table metadata for table: " + tableName, ex);
            }

            tableMetaDataMap.put(tableName, tableMetaData);
        }

        // Create package-info.java
        if (output.isPackageInfo()) {
            final CompilationUnit packageInfo = packageInfoGenerator.createPackageInfo(output.getOutputPackage(), "Litebridge entities.");
            javaFileWriter.writeJavaFile(output.getOutputPackage(), "package-info.java", packageInfo);
        }

        // Extract many-to-many mappings
        final List<ManyToManyMapping> manyToManyMappings = ManyToManyMapper.extractManyToManyMappings(tableMetaDataMap);

        // Generate entities
        final Map<String, GeneratedEntity> entities = new HashMap<>(input.getTables().size());

        for (final String tableName : input.getTables()) {
            final TableMetaData tableMetaData = tableMetaDataMap.get(tableName);
            final GeneratedEntity generatedEntity = entityGenerator.createEntityClassForTable(tableMetaData, tableMetaDataMap, manyToManyMappings, entities);
            javaFileWriter.writeEntityJavaFile(generatedEntity);
        }
    }

    private void validateConfig() throws MojoExecutionException {
        if (getLog().isDebugEnabled()) {
            getLog().debug("Database configuration: " + database);
            getLog().debug("Input configuration: " + input);
            getLog().debug("Output configuration: " + output);
        }

        ObjectUtils.requireNonNull(database, () -> new MojoExecutionException("Mandatory config parameter missing: <database>"));
        ObjectUtils.requireNonNull(input, () -> new MojoExecutionException("Mandatory config parameter missing: <input>"));
        ObjectUtils.requireNonNull(output, () -> new MojoExecutionException("Mandatory config parameter missing: <output>"));
    }

    @SuppressWarnings("unchecked")
    private TransactionalDatabaseProvider createDatabaseProvider(final DataSource dataSource) {
        if (getLog().isDebugEnabled()) {
            getLog().debug("Creating database provider instance for class: " + database.getDatabaseProviderClass());
        }

        final Class<? extends DatabaseProvider> databaseProviderClass;

        try {
            final Class<?> candidateClass = Class.forName(this.database.getDatabaseProviderClass());

            if (DatabaseProvider.class.isAssignableFrom(candidateClass)) {
                databaseProviderClass = (Class<? extends DatabaseProvider>) candidateClass;
            } else {
                throw new IllegalArgumentException("Failed to setup Litebridge reverse engineering: Specified class does not implement DatabaseProvider: %s".formatted(database.getDatabaseProviderClass()));
            }
        } catch (ClassNotFoundException ex) {
            throw new IllegalArgumentException("Failed to setup Litebridge reverse engineering: DatabaseProvider class not found: %s".formatted(database.getDatabaseProviderClass()), ex);
        }

        return new TransactionalDatabaseProvider(new DefaultTransactionManager(dataSource), ClassUtils.newInstance(databaseProviderClass));
    }
}
