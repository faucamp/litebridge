package org.litebridge.spring.boot.autoconfigure;

import org.flywaydb.core.Flyway;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.litebridge.commons.ClassUtils;
import org.litebridge.db.spi.DatabaseMetaData;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.DatabaseProviderMetaData;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.expression.SqlFunctionRegistry;
import org.litebridge.db.spi.generator.SequenceColumnValueGenerator;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.db.spi.tx.ConnectionProvider;
import org.litebridge.db.spi.tx.TransactionManager;
import org.litebridge.db.spi.update.BatchUpdateResult;
import org.litebridge.db.spi.update.Result;
import org.litebridge.orm.Litebridge;
import org.litebridge.orm.LitebridgeBuilder;
import org.litebridge.orm.LitebridgeCore;
import org.litebridge.orm.config.LitebridgeConfig;
import org.litebridge.orm.config.RelatedDtoStrategy;
import org.litebridge.orm.spi.LitebridgeOverrideDatabaseProvider;
import org.litebridge.spring.LitebridgeTransactionManager;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.lang.invoke.MethodHandles;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LitebridgeAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(LitebridgeAutoConfiguration.class))
            .withUserConfiguration(MockDataSourceConfig.class);

    private final ApplicationContextRunner contextRunnerH2 = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(LitebridgeAutoConfiguration.class))
            .withUserConfiguration(H2DataSourceConfig.class);

    @Test
    void autoConfigure_configDatabaseProvider() {
        this.contextRunner
                .withPropertyValues("litebridge.database-provider.class=org.litebridge.db.h2.H2DatabaseProvider")
                .run(context -> {
                    assertThat(context).hasSingleBean(Litebridge.class);
                    assertThat(context).hasSingleBean(LitebridgeTransactionManager.class);
                });
    }

    @Test
    void autoConfigure_configDatabaseProvider_noConstructor() {
        this.contextRunner
                .withPropertyValues("litebridge.database-provider.class=org.litebridge.spring.boot.autoconfigure.LitebridgeAutoConfigurationTest$NoConstructorDatabaseProvider")
                .run(context -> {
                    assertThat(context).hasFailed();
                });
    }

    @Test
    void autoConfigure_configDatabaseProvider_providerClassNotFound() {
        this.contextRunner
                .withPropertyValues("litebridge.database-provider.class=com.example.NonExistentProvider")
                .run(context -> {
                    assertThat(context).hasFailed();
                });
    }

    @Test
    void autoConfigure_configDatabaseProvider_invalidProviderClass() {
        this.contextRunner
                .withPropertyValues("litebridge.database-provider.class=java.lang.String")
                .run(context -> {
                    assertThat(context).hasFailed();
                });
    }

    @Test
    void autoConfigure_classpathDatabaseProvider() {
        this.contextRunner
                .run(context -> {
                    assertThat(context).hasSingleBean(Litebridge.class);
                    assertThat(context).hasSingleBean(LitebridgeTransactionManager.class);
                });
    }

    @Test
    void autoConfigure_classpathDatabaseProvider_noProviderFound() {
        this.contextRunner
                .withPropertyValues("litebridge.database-provider.scan-base-package=com.example.nonexistent")
                .run(context -> {
                    assertThat(context).hasFailed();
                });
    }

    @Test
    void autoConfigure_classpathDatabaseProvider_multipleProvidersFound() {
        this.contextRunner
                .withPropertyValues("litebridge.database-provider.scan-base-package=org.litebridge")
                .run(context -> {
                    assertThat(context).hasFailed();
                });
    }

    static class MockDataSourceConfig {
        @Bean
        public DataSource dataSource() throws SQLException {
            final DataSource dataSource = mock(DataSource.class);
            final Connection connection = mock(Connection.class);
            final java.sql.DatabaseMetaData metaData = mock(java.sql.DatabaseMetaData.class);
            when(dataSource.getConnection()).thenReturn(connection);
            when(connection.getMetaData()).thenReturn(metaData);
            when(metaData.getDatabaseProductName()).thenReturn("H2");
            when(metaData.getDatabaseProductVersion()).thenReturn("2.3.232");
            when(metaData.getDriverName()).thenReturn("H2 JDBC Driver");
            when(metaData.getDriverVersion()).thenReturn("2.3.232");
            return dataSource;
        }
    }

    static class H2DataSourceConfig {

        @Bean
        public DataSource dataSource() {
            final DataSource dataSource = new DriverManagerDataSource("jdbc:h2:mem:lb;DB_CLOSE_DELAY=-1", "sa", "");
            final Flyway flyway = Flyway.configure().dataSource(dataSource).load();
            flyway.migrate();
            return dataSource;
        }
    }

    @Test
    void autoConfigure_scanBasePackage_noEntitiesOrMappingsFound() {
        this.contextRunner
                .withPropertyValues(
                        "litebridge.database-provider.class=org.litebridge.db.h2.H2DatabaseProvider",
                        "litebridge.scan-base-package=com.example.nonexistent"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(Litebridge.class);
                    assertThat(context).hasSingleBean(LitebridgeTransactionManager.class);
                });
    }

    @Test
    void autoConfigure_scanBasePackage_registersEntityClasses() {
        this.contextRunnerH2
                .withPropertyValues(
                        "litebridge.database-provider.class=org.litebridge.db.h2.H2DatabaseProvider",
                        "litebridge.scan-base-package=org.litebridge.spring.boot.autoconfigure.test.entity"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(Litebridge.class);
                    assertThat(context).hasSingleBean(LitebridgeTransactionManager.class);
                });
    }

    @Test
    void autoConfigure_scanBasePackage_registersTypeSafeMappings() {
        this.contextRunnerH2
                .withPropertyValues(
                        "litebridge.database-provider.class=org.litebridge.db.h2.H2DatabaseProvider",
                        "litebridge.scan-base-package=org.litebridge.spring.boot.autoconfigure.test.mapping"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(Litebridge.class);
                    assertThat(context).hasSingleBean(LitebridgeTransactionManager.class);
                });
    }

    @Test
    void autoConfigure_appliesConfigurerWhenPresent() {
        // Given
        final AtomicBoolean configured = new AtomicBoolean(false);

        // When / Then
        this.contextRunner
                .withBean(LitebridgeConfigurer.class, () -> litebridge -> configured.set(true))
                .withPropertyValues("litebridge.database-provider.class=org.litebridge.db.h2.H2DatabaseProvider")
                .run(context -> {
                    assertThat(context).hasSingleBean(Litebridge.class);
                    context.getBean(Litebridge.class);
                    assertThat(configured).isTrue();
                });
    }

    @Test
    void autoConfigure_customLitebridgeBean_overridesFactoryBean() {
        // Given
        final Litebridge customLitebridge = mock(Litebridge.class);

        // When / Then
        this.contextRunner
                .withBean(Litebridge.class, () -> customLitebridge)
                .withPropertyValues("litebridge.database-provider.class=org.litebridge.db.h2.H2DatabaseProvider")
                .run(context -> {
                    assertThat(context).hasSingleBean(Litebridge.class);
                    assertThat(context.getBean(Litebridge.class)).isSameAs(customLitebridge);
                    assertThat(context).doesNotHaveBean(LitebridgeFactoryBean.class);
                });
    }

    @Test
    void autoConfigure_customLitebridgeCoreBean_overridesFactoryBean() {
        // Given
        final LitebridgeCore customLitebridgeCore = mock(LitebridgeCore.class);

        // When / Then
        this.contextRunner
                .withBean(LitebridgeCore.class, () -> customLitebridgeCore)
                .withPropertyValues("litebridge.database-provider.class=org.litebridge.db.h2.H2DatabaseProvider")
                .run(context -> {
                    assertThat(context).hasSingleBean(LitebridgeCore.class);
                    assertThat(context.getBean(LitebridgeCore.class)).isSameAs(customLitebridgeCore);
                    assertThat(context).doesNotHaveBean(LitebridgeFactoryBean.class);
                });
    }

    @Test
    void autoConfigure_overrideDatabaseProvider_registersExactBeanType() {
        // When / Then
        this.contextRunner
                .withPropertyValues("litebridge.database-provider.class=org.litebridge.spring.boot.autoconfigure.LitebridgeAutoConfigurationTest$TestOverrideDatabaseProvider")
                .run(context -> {
                    assertThat(context).hasSingleBean(LitebridgeCore.class);
                    assertThat(context.getBean(LitebridgeCore.class)).isInstanceOf(LitebridgeCore.class);
                    assertThat(context).doesNotHaveBean(Litebridge.class);
                    assertThat(context).hasSingleBean(LitebridgeTransactionManager.class);
                });
    }

    @Test
    void autoConfigure_customLitebridgeSubtypeDatabaseProvider_registersExactBeanType() {
        // When / Then
        this.contextRunner
                .withPropertyValues("litebridge.database-provider.class=org.litebridge.spring.boot.autoconfigure.LitebridgeAutoConfigurationTest$TestCustomLitebridgeDatabaseProvider")
                .run(context -> {
                    assertThat(context).hasSingleBean(CustomLitebridge.class);
                    assertThat(context.getBean(CustomLitebridge.class)).isInstanceOf(CustomLitebridge.class);
                    assertThat(context).hasSingleBean(LitebridgeTransactionManager.class);
                });
    }

    @Test
    void autoConfigure_relatedDtoStrategy() {
        this.contextRunner
                .withPropertyValues(
                        "litebridge.database-provider.class=org.litebridge.db.h2.H2DatabaseProvider",
                        "litebridge.related-dto-strategy=PARTIAL_OBJECT_IF_NO_JOIN"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(Litebridge.class);
                    final Litebridge litebridge = context.getBean(Litebridge.class);

                    // Verify via reflection since it's not exposed
                    final java.lang.reflect.Field configField = ClassUtils.getField(Litebridge.class, "litebridgeConfig");
                    configField.setAccessible(true);
                    final LitebridgeConfig config = (LitebridgeConfig) configField.get(litebridge);
                    assertThat(config.relatedDtoStrategy()).isEqualTo(RelatedDtoStrategy.PARTIAL_OBJECT_IF_NO_JOIN);
                });
    }

    public static class CustomLitebridge extends LitebridgeCore {
        public CustomLitebridge(final DatabaseProvider databaseProvider,
                                final TransactionManager transactionManager,
                                final LitebridgeConfig litebridgeConfig,
                                final MethodHandles.Lookup lookup) {
            super(databaseProvider, transactionManager, litebridgeConfig, lookup);
        }
    }

    public static class TestDatabaseProvider implements DatabaseProvider {
        private final DatabaseProvider delegate = new org.litebridge.db.h2.H2DatabaseProvider();

        @Override
        public DatabaseProviderMetaData metaData() {
            return delegate.metaData();
        }

        @Override
        public DatabaseMetaData databaseMetaData(final @NonNull ConnectionProvider connectionProvider) throws SQLException {
            return delegate.databaseMetaData(connectionProvider);
        }

        @Override
        public TableMetaData tableMetaData(final @NonNull Table table, final @NonNull ConnectionProvider connectionProvider) throws SQLException {
            return delegate.tableMetaData(table, connectionProvider);
        }

        @Override
        public <T extends Result> T executeUpdate(final @NonNull PreparedSql preparedSql, final @NonNull Class<T> resultType, final @NonNull ConnectionProvider connectionProvider) throws SQLException {
            return delegate.executeUpdate(preparedSql, resultType, connectionProvider);
        }

        @Override
        public BatchUpdateResult executeBatch(final @NonNull List<PreparedSql> preparedSql, final @NonNull ConnectionProvider connectionProvider) throws SQLException {
            return delegate.executeBatch(preparedSql, connectionProvider);
        }

        @Override
        public List<Row> executeQuery(final @NonNull PreparedSql preparedSql, final @NonNull ConnectionProvider connectionProvider) throws SQLException {
            return delegate.executeQuery(preparedSql, connectionProvider);
        }

        @Override
        public String toSql(final @NonNull Operation operation, final @NonNull ConnectionProvider connectionProvider) {
            return delegate.toSql(operation, connectionProvider);
        }

        @Override
        public TypeConverter typeConverter() {
            return delegate.typeConverter();
        }

        @Override
        public SequenceColumnValueGenerator sequenceColumnValueGenerator(final @NonNull String sequence) throws UnsupportedOperationException {
            return delegate.sequenceColumnValueGenerator(sequence);
        }

        @Override
        public SqlFunctionRegistry sqlFunctionRegistry() {
            return delegate.sqlFunctionRegistry();
        }
    }

    public static class TestOverrideDatabaseProvider extends TestDatabaseProvider implements LitebridgeOverrideDatabaseProvider<LitebridgeCore> {
        public TestOverrideDatabaseProvider() {
        }

        @Override
        public Class<LitebridgeCore> litebridgeClass() {
            return LitebridgeCore.class;
        }
    }

    public static class TestCustomLitebridgeDatabaseProvider extends TestDatabaseProvider implements LitebridgeOverrideDatabaseProvider<CustomLitebridge> {
        public TestCustomLitebridgeDatabaseProvider() {
        }

        @Override
        public Class<CustomLitebridge> litebridgeClass() {
            return CustomLitebridge.class;
        }

        @Override
        public CustomLitebridge createLitebridge(final LitebridgeBuilder.ConstructorArgs constructorArgs) {
            return new CustomLitebridge(
                    constructorArgs.databaseProvider(),
                    constructorArgs.transactionManager(),
                    constructorArgs.litebridgeConfig(),
                    constructorArgs.lookup()
            );
        }
    }

    public static class NoConstructorDatabaseProvider implements DatabaseProvider {

        private NoConstructorDatabaseProvider() {
        }

        @Override
        public DatabaseProviderMetaData metaData() {
            return null;
        }

        @Override
        public DatabaseMetaData databaseMetaData(final @NonNull ConnectionProvider connectionProvider) throws SQLException {
            return null;
        }

        @Override
        public TableMetaData tableMetaData(final @NonNull Table table, final @NonNull ConnectionProvider connectionProvider) throws SQLException {
            return null;
        }

        @Override
        public <T extends Result> T executeUpdate(final @NonNull PreparedSql preparedSql, final @NonNull Class<T> resultType, final @NonNull ConnectionProvider connectionProvider) throws SQLException {
            return null;
        }

        @Override
        public BatchUpdateResult executeBatch(final @NonNull List<PreparedSql> preparedSql, final @NonNull ConnectionProvider connectionProvider) throws SQLException {
            return null;
        }

        @Override
        public List<Row> executeQuery(final @NonNull PreparedSql preparedSql, final @NonNull ConnectionProvider connectionProvider) throws SQLException {
            return List.of();
        }

        @Override
        public String toSql(final @NonNull Operation operation, final @NonNull ConnectionProvider connectionProvider) {
            return "";
        }

        @Override
        public TypeConverter typeConverter() {
            return null;
        }

        @Override
        public SequenceColumnValueGenerator sequenceColumnValueGenerator(final @NonNull String sequence) throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        @Override
        public SqlFunctionRegistry sqlFunctionRegistry() {
            return null;
        }
    }
}
