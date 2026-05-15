package org.litebridgedb.spring.boot.autoconfigure;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.DatabaseProvider;
import org.litebridgedb.db.spi.Row;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.TableMetaData;
import org.litebridgedb.db.spi.convert.TypeConverter;
import org.litebridgedb.db.spi.query.Select;
import org.litebridgedb.db.spi.tx.ConnectionProvider;
import org.litebridgedb.db.spi.update.Delete;
import org.litebridgedb.db.spi.update.Insert;
import org.litebridgedb.db.spi.update.InsertResult;
import org.litebridgedb.db.spi.update.Update;
import org.litebridgedb.db.spi.update.UpdateResult;
import org.litebridgedb.orm.Litebridge;
import org.litebridgedb.spring.LitebridgeTransactionManager;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class LitebridgeAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(LitebridgeAutoConfiguration.class))
            .withUserConfiguration(TestConfig.class);

    @Test
    void autoConfigure_configDatabaseProvider() {
        this.contextRunner
                .withPropertyValues("litebridgedb.database-provider.class=org.litebridgedb.db.h2.H2DatabaseProvider")
                .run(context -> {
                    assertThat(context).hasSingleBean(Litebridge.class);
                    assertThat(context).hasSingleBean(LitebridgeTransactionManager.class);
                });
    }

    @Test
    void autoConfigure_configDatabaseProvider_noConstructor() {
        this.contextRunner
                .withPropertyValues("litebridgedb.database-provider.class=org.litebridgedb.spring.boot.autoconfigure.LitebridgeAutoConfigurationTest$NoConstructorDatabaseProvider")
                .run(context -> {
                    assertThat(context).hasFailed();
                });
    }

    @Test
    void autoConfigure_configDatabaseProvider_providerClassNotFound() {
        this.contextRunner
                .withPropertyValues("litebridgedb.database-provider.class=com.example.NonExistentProvider")
                .run(context -> {
                    assertThat(context).hasFailed();
                });
    }

    @Test
    void autoConfigure_configDatabaseProvider_invalidProviderClass() {
        this.contextRunner
                .withPropertyValues("litebridgedb.database-provider.class=java.lang.String")
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
                .withPropertyValues("litebridgedb.database-provider.scan-base-package=com.example.nonexistent")
                .run(context -> {
                    assertThat(context).hasFailed();
                });
    }

    @Test
    void autoConfigure_classpathDatabaseProvider_multipleProvidersFound() {
        this.contextRunner
                .withPropertyValues("litebridgedb.database-provider.scan-base-package=org.litebridgedb")
                .run(context -> {
                    assertThat(context).hasFailed();
                });
    }

    @Configuration
    static class TestConfig {
        @Bean
        public DataSource dataSource() {
            return mock(DataSource.class);
        }
    }

    /**
     * Class designed to test inaccessibility of constructor
     */
    public static class NoConstructorDatabaseProvider implements DatabaseProvider {

        private NoConstructorDatabaseProvider() {
        }

        @Override
        public TableMetaData tableMetaData(final Table table, final ConnectionProvider connectionProvider) throws SQLException {
            return null;
        }

        @Override
        public InsertResult insert(final Insert insert, final ConnectionProvider connectionProvider) throws SQLException {
            return null;
        }

        @Override
        public UpdateResult update(final Update update, final ConnectionProvider connectionProvider) throws SQLException {
            return null;
        }

        @Override
        public List<Row> select(final Select select, final ConnectionProvider connectionProvider) throws SQLException {
            return List.of();
        }

        @Override
        public UpdateResult delete(final Delete delete, final ConnectionProvider connectionProvider) throws SQLException {
            return null;
        }

        @Override
        public String toSql(final Select select) {
            return "";
        }

        @Override
        public TypeConverter getTypeConverter() {
            return null;
        }
    }
}
