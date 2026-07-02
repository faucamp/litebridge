package org.litebridgedb.maven;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.jspecify.annotations.Nullable;
import org.litebridgedb.commons.ClassUtils;
import org.litebridgedb.commons.CollectionUtils;
import org.litebridgedb.commons.ObjectUtils;
import org.litebridgedb.commons.StringUtils;
import org.litebridgedb.convert.DefaultTypeConverter;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.DatabaseProvider;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.TableMetaData;
import org.litebridgedb.db.spi.convert.TypeConverter;
import org.litebridgedb.maven.config.ColumnMappingConfig;
import org.litebridgedb.maven.config.DatabaseConfig;
import org.litebridgedb.maven.config.InputConfig;
import org.litebridgedb.maven.config.OutputConfig;
import org.litebridgedb.maven.config.SqlTypeMappingConfig;
import org.litebridgedb.maven.config.TableMappingConfig;
import org.litebridgedb.orm.annotation.Column;
import org.litebridgedb.orm.persistence.TransactionalDatabaseProvider;
import org.litebridgedb.orm.tx.DefaultTransactionManager;
import org.litebridgedb.orm.tx.LitebridgeDriverManagerDataSource;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mojo(name = "reverse-engineer", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public final class ReverseEngineerMojo extends AbstractMojo {

    /**
     * Maven project instance
     */
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

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
    private InputConfig input;

    /**
     * SQL data type override configuration.
     * <p>
     * This is an optional global setting that allows specifying exact Java classes to use for SQL data types.
     */
    @Parameter
    private List<SqlTypeMappingConfig> sqlTypeMappings;

    /**
     * Additional table mapping configuration.
     * <p>
     * This is optional, but allows setting up inter-table relationships, per-column data type overrides, etc.
     */
    @Parameter
    private List<TableMappingConfig> tableMappings;

    /**
     * Generated output configuration.
     */
    @Parameter(required = true)
    private OutputConfig output;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Reverse engineering Litebridge entities");
        validateConfig();

        if (getLog().isDebugEnabled()) {
            getLog().debug("Creating datasource for URL: " + database.getUrl());
        }

        final LitebridgeDriverManagerDataSource dataSource = new LitebridgeDriverManagerDataSource(database.getUrl(), database.getUser(), database.getPassword());
        final TransactionalDatabaseProvider databaseProvider = createDatabaseProvider(dataSource);

        for (final String tableName : input.getTables()) {
            final TableMetaData tableMetaData;

            try {
                tableMetaData = databaseProvider.tableMetaData(new Table(tableName), databaseProvider.transactionManager());
            } catch (SQLException ex) {
                throw new MojoExecutionException("Failed to retrieve table metadata for table: " + tableName, ex);
            }

            createEntityClassForTable(tableMetaData);
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

    private void createEntityClassForTable(final TableMetaData tableMetaData) throws MojoExecutionException {
        final TypeConverter typeConverter = new DefaultTypeConverter();
        final TableMappingConfig tableMappingConfig;

        if (!CollectionUtils.isEmpty(tableMappings)) {
            tableMappingConfig = tableMappings.stream()
                    .filter(t -> t.getTable().equals(tableMetaData.qualifiedName()))
                    .findFirst().orElse(null);
        } else {
            tableMappingConfig = null;
        }

        final String entityClassName;

        if (tableMappingConfig != null && tableMappingConfig.getEntityName() != null) {
            entityClassName = tableMappingConfig.getEntityName();
        } else {
            entityClassName = camelCase(tableMetaData.name(), false);
        }

        final CompilationUnit entity = new CompilationUnit();
        entity.setPackageDeclaration(output.getOutputPackage())
                .addImport(org.litebridgedb.orm.annotation.Table.class)
                .addImport(Column.class);
        final ClassOrInterfaceDeclaration entityClass = entity.addClass(entityClassName)
                .setPublic(true)
                .setFinal(output.isFinalClasses())
                .addSingleMemberAnnotation(org.litebridgedb.orm.annotation.Table.class.getSimpleName(),
                        "\"%s\"".formatted(tableMetaData.qualifiedName()));

        if (output.isJavadoc()) {
            entityClass.setJavadocComment("Entity class for table: {@code %s}".formatted(tableMetaData.qualifiedName()));
        }

        // Create fields for columns
        final List<FieldDeclaration> declaredFields = new ArrayList<>(tableMetaData.columns().size());

        for (ColumnMetaData columnMetaData : tableMetaData.columns()) {
            // Create the entity field
            final ColumnMappingConfig columnMappingConfig;
            final String fieldName;
            Class<?> fieldClass = null;

            if (tableMappingConfig != null && tableMappingConfig.getColumnMappings() != null) {
                columnMappingConfig = tableMappingConfig.getColumnMappings().stream()
                        .filter(c -> c.getColumn().equals(columnMetaData.name()))
                        .findFirst().orElse(null);

                if (columnMappingConfig != null) {
                    if (columnMappingConfig.getFieldType() != null) {
                        try {
                            fieldClass = PrimitiveLookup.getPrimitiveClass(columnMappingConfig.getFieldType());
                        } catch (ClassNotFoundException ex) {
                            throw new MojoExecutionException("Failed to load field type class '%s' for column mapping: %s".formatted(columnMappingConfig.getFieldType(), columnMetaData.name()));
                        }
                    }

                    if (columnMappingConfig.getFieldName() != null) {
                        fieldName = columnMappingConfig.getFieldName();
                    } else {
                        fieldName = camelCase(columnMetaData.name(), true);
                    }
                } else {
                    fieldName = camelCase(columnMetaData.name(), true);
                }
            } else {
                columnMappingConfig = null;
                fieldName = camelCase(columnMetaData.name(), true);
            }

            // Field type
            if (fieldClass == null) {
                fieldClass = sqlTypeMappings.stream()
                        .filter(m -> m.getSqlType().getVendorTypeNumber().equals(columnMetaData.getDataType()))
                        .filter(m -> m.getPrecision() == null || m.getPrecision().equals(columnMetaData.getSize()))
                        .filter(m -> m.getNotNull() == null || m.getNotNull().equals(!columnMetaData.isNullable()))
                        .reduce((a, b) -> {
                            if (a.getPrecision() != null) {
                                if (b.getPrecision() != null) {
                                    if (a.getNotNull() != null) {
                                        return a;
                                    }

                                    return b;
                                }

                                return a;
                            }

                            if (b.getPrecision() != null) {
                                return b;
                            }

                            if (b.getNotNull() != null) {
                                if (a.getNotNull() != null) {
                                    return a;
                                }

                                return b;
                            }

                            return a;
                        })
                        .map(sqlTypeMappingConfig -> {
                            try {
                                return PrimitiveLookup.getPrimitiveClass(sqlTypeMappingConfig.getFieldType());
                            } catch (ClassNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .orElseGet(() -> (Class) typeConverter.getClassForSqlType(columnMetaData.getDataType()));
            }

            final FieldDeclaration field = entityClass.addField(
                            fieldClass,
                            fieldName,
                            Modifier.Keyword.PRIVATE)
                    .addAnnotation(createColumnAnnotation(columnMetaData, columnMappingConfig));

            if (output.isJavadoc()) {
                field.setJavadocComment("Column: {@code %s}".formatted(columnMetaData.name()));
            }

            declaredFields.add(field);
        }

        // Add getters/setters
        final List<String> fieldNames = new ArrayList<>(declaredFields.size());

        declaredFields.forEach(field -> {
            fieldNames.add(field.getVariable(0).getNameAsString());
            field.createGetter();
            field.createSetter();
        });

        // Add equals(), hashcodde() and toString()
        entityClass.addMember(createEquals(entityClassName, fieldNames));
        entityClass.addMember(createHashCode(fieldNames));
        entityClass.addMember(createToString(entityClassName, fieldNames));

        // Write generated entity class to file
        final String[] targetPackageArray = Stream.concat(
                        Stream.of("generated-sources", "java"),
                        Arrays.stream(StringUtils.splitArray(output.getOutputPackage(), '.', -1, false)))
                .toArray(String[]::new);
        final Path packagePath = Paths.get(project.getBuild().getDirectory(), targetPackageArray);
        final File targetFile = new File(packagePath.toFile(), "%s.java".formatted(entityClassName));

        try {
            Files.createDirectories(packagePath);

            try (FileWriter writer = new FileWriter(targetFile)) {
                writer.write(entity.toString());
            }

            getLog().info("Created Litebridge entity class %s for table %s at: %s".formatted(entityClassName, tableMetaData.qualifiedName(), targetFile.getAbsolutePath()));
        } catch (IOException ex) {
            throw new MojoExecutionException("Error writing generated Java class to file", ex);
        }
    }

    private AnnotationExpr createColumnAnnotation(final ColumnMetaData columnMetaData, final @Nullable ColumnMappingConfig columnMappingConfig) {
        final NormalAnnotationExpr annotation = new NormalAnnotationExpr();
        annotation.setName(new Name(Column.class.getSimpleName()));
        annotation.addPair("value", "\"%s\"".formatted(columnMetaData.name()));

        if (columnMappingConfig == null) {
            return annotation;
        }

        if (!StringUtils.isBlank(columnMappingConfig.getGenerateUsingSequence())) {
            annotation.addPair("generateUsingSequence", "\"%s\"".formatted(columnMappingConfig.getGenerateUsingSequence()));
        }

        if (!StringUtils.isBlank(columnMappingConfig.getGeneratorClass())) {
            annotation.addPair("generator", columnMappingConfig.getGeneratorClass());
        }

        return annotation;
    }

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
                throw new IllegalArgumentException("Failed to setup Litebridge Reverse Engineering: Specified class does not implement DatabaseProvider: %s".formatted(database.getDatabaseProviderClass()));
            }
        } catch (ClassNotFoundException ex) {
            throw new IllegalArgumentException("Failed to setup Litebridge Reverse Engineering: DatabaseProvider class not found: %s".formatted(database.getDatabaseProviderClass()), ex);
        }

        return new TransactionalDatabaseProvider(new DefaultTransactionManager(dataSource), ClassUtils.newInstance(databaseProviderClass));
    }

    /**
     * Converts the given string into camelCase format by removing non-word characters,
     * Lowercasing the first word if {@code lowercaseFirst} is {@code true},
     * and capitalizing the first letter of subsequent words.
     *
     * @param str the input string to be converted; must not be null
     * @return the camelCase formatted string, or an empty string if the input is empty or contains only non-word characters
     * @throws NullPointerException if the input string is null
     */
    private static String camelCase(final String str, final boolean lowercaseFirst) {
        Objects.requireNonNull(str, "Input cannot be null");

        // Split the string by any non-word characters (including spaces and underscores)
        final String[] words = str.split("[\\W_]+");
        final StringBuilder builder = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            final String word = words[i];

            if (lowercaseFirst && i == 0) {
                // For the first word, convert to lowercase
                builder.append(word.toLowerCase());
            } else {
                // For subsequent words, capitalize the first letter and lowercase the rest
                builder.append(Character.toUpperCase(word.charAt(0)));
                builder.append(word.substring(1).toLowerCase());
            }
        }

        return builder.toString();
    }

    private static MethodDeclaration createToString(final String className, final List<String> fields) {
        // Build the string concatenation expression for the return statement
        // Example output format: "User{id=" + id + ", name='" + name + "', active=" + active + "}"
        final String returnExpression = fields.stream()
                .map(field -> "\"" + field + "=\" + " + field)
                .collect(Collectors.joining(" + \", \" + ", "\"" + className + "{\" + ", " + \"}\""));

        // Create the toString method declaration
        final MethodDeclaration toStringMethod = new MethodDeclaration()
                .setModifiers(Modifier.Keyword.PUBLIC)
                .setType(String.class)
                .setName("toString")
                .addAnnotation(Override.class); // Adds @Override annotation

        // Define the method body with the generated return statement
        BlockStmt body = new BlockStmt();
        body.addStatement("return " + returnExpression + ";");
        toStringMethod.setBody(body);

        return toStringMethod;
    }

    private static MethodDeclaration createEquals(final String className, final List<String> fields) {
        // Build the basic method signature: public boolean equals(Object obj)
        MethodDeclaration equalsMethod = new MethodDeclaration()
                .setModifiers(Modifier.Keyword.PUBLIC)
                .setType("boolean")
                .setName("equals")
                .addParameter(new com.github.javaparser.ast.body.Parameter(StaticJavaParser.parseType("Object"), "obj"));

        equalsMethod.addAnnotation("Override");

        // Build the inner logic block using template strings
        StringBuilder body = new StringBuilder("{\n");
        body.append("    if (this == obj) return true;\n");
        body.append("    if (obj == null || getClass() != obj.getClass()) return false;\n");
        body.append(String.format("    %s other = (%s) obj;\n", className, className));

        // Use Objects.equals for safe null/primitive checks
        String comparisons = fields.stream()
                .map(field -> String.format("java.util.Objects.equals(%s, other.%s)", field, field))
                .collect(Collectors.joining("\n        && "));

        body.append("    return ").append(comparisons).append(";\n");
        body.append("}");

        equalsMethod.setBody(StaticJavaParser.parseBlock(body.toString()));
        return equalsMethod;
    }

    private static MethodDeclaration createHashCode(List<String> fields) {
        // Build basic method signature: public int hashCode()
        MethodDeclaration hashCodeMethod = new MethodDeclaration()
                .setModifiers(Modifier.Keyword.PUBLIC)
                .setType("int")
                .setName("hashCode");

        hashCodeMethod.addAnnotation("Override");

        // Use java.util.Objects.hash() for a clean builder approach
        String fieldsCsv = String.join(", ", fields);
        String body = String.format("{\n    return java.util.Objects.hash(%s);\n}", fieldsCsv);

        hashCodeMethod.setBody(StaticJavaParser.parseBlock(body));
        return hashCodeMethod;
    }
}
