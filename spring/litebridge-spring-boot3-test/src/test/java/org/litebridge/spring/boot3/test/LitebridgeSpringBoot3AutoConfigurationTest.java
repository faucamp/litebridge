package org.litebridge.spring.boot3.test;

import org.flywaydb.core.Flyway;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.alias.AliasTransformer;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.expression.SqlFunctionRegistry;
import org.litebridge.db.spi.generator.SequenceColumnValueGenerator;
import org.litebridge.db.spi.query.Select;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.db.spi.tx.ConnectionProvider;
import org.litebridge.db.spi.update.InsertResult;
import org.litebridge.db.spi.update.UpdateResult;
import org.litebridge.orm.Litebridge;
import org.litebridge.spring.LitebridgeTransactionManager;
import org.litebridge.spring.boot.autoconfigure.LitebridgeAutoConfiguration;
import org.litebridge.spring.boot.autoconfigure.LitebridgeConfigurer;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class LitebridgeSpringBoot3AutoConfigurationTest {

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
                .withPropertyValues("litebridge.database-provider.class=org.litebridge.spring.boot3.test.LitebridgeSpringBoot3AutoConfigurationTest$NoConstructorDatabaseProvider")
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
        public DataSource dataSource() {
            return mock(DataSource.class);
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
        final AtomicBoolean configured = new AtomicBoolean(false);

        this.contextRunner
                .withBean(LitebridgeConfigurer.class, () -> litebridge -> configured.set(true))
                .withPropertyValues("litebridge.database-provider.class=org.litebridge.db.h2.H2DatabaseProvider")
                .run(context -> {
                    assertThat(context).hasSingleBean(Litebridge.class);
                    assertThat(configured).isTrue();
                });
    }

    public static class NoConstructorDatabaseProvider implements DatabaseProvider {

        private NoConstructorDatabaseProvider() {
        }

        @Override
        public TableMetaData tableMetaData(final Table table, final ConnectionProvider connectionProvider) throws SQLException {
            return null;
        }

        @Override
        public InsertResult insert(final PreparedOperation insert, final ConnectionProvider connectionProvider) throws SQLException {
            return null;
        }

        @Override
        public UpdateResult update(final PreparedOperation update, final ConnectionProvider connectionProvider) throws SQLException {
            return null;
        }

        @Override
        public List<Row> select(final PreparedSql preparedSql, final ConnectionProvider connectionProvider) throws SQLException {
            return List.of();
        }

        @Override
        public UpdateResult delete(final PreparedOperation delete, final ConnectionProvider connectionProvider) throws SQLException {
            return null;
        }

        @Override
        public String toSql(final Operation operation, final ConnectionProvider connectionProvider) {
            return "";
        }

        @Override
        public List<Row> nativeSqlQuery(final String sql, final List<@Nullable Object> bindParameters, final ConnectionProvider connectionProvider) throws SQLException {
            return null;
        }

        @Override
        public UpdateResult nativeSqlUpdate(final String sql, final List<@Nullable Object> bindParameters, final ConnectionProvider connectionProvider) throws SQLException {
            return null;
        }

        @Override
        public TypeConverter getTypeConverter() {
            return null;
        }

        @Override
        public SequenceColumnValueGenerator getSequenceColumnValueGenerator(final String sequence) throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        @Override
        public SqlFunctionRegistry getSqlFunctionRegistry() {
            return null;
        }

        @Override
        public AliasTransformer getAliasTransformer() {
            return null;
        }
    }
}
