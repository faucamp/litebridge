package org.litebridge.spring.boot.autoconfigure;

import org.junit.jupiter.api.Test;
import org.litebridge.orm.Litebridge;
import org.litebridge.spring.LitebridgeTransactionManager;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class LitebridgeAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(LitebridgeAutoConfiguration.class))
            .withUserConfiguration(TestConfig.class);

    @Test
    void shouldAutoConfigureLitebridge() {
        this.contextRunner
                .withPropertyValues("litebridge.database-provider-class=org.litebridge.spring.boot.autoconfigure.LitebridgeAutoConfigurationTest$MockDatabaseProvider")
                .run(context -> {
                    assertThat(context).hasSingleBean(Litebridge.class);
                    assertThat(context).hasSingleBean(LitebridgeTransactionManager.class);
                });
    }

    @Configuration
    static class TestConfig {
        @Bean
        public DataSource dataSource() {
            return mock(DataSource.class);
        }
    }

    public static class MockDatabaseProvider implements org.litebridge.db.spi.DatabaseProvider {
        @Override public org.litebridge.db.spi.TableMetaData tableMetaData(org.litebridge.db.spi.Table table, org.litebridge.db.spi.tx.ConnectionProvider connectionProvider) { return null; }
        @Override public org.litebridge.db.spi.update.InsertResult insert(org.litebridge.db.spi.update.Insert insert, org.litebridge.db.spi.tx.ConnectionProvider connectionProvider) { return null; }
        @Override public org.litebridge.db.spi.update.UpdateResult update(org.litebridge.db.spi.update.Update update, org.litebridge.db.spi.tx.ConnectionProvider connectionProvider) { return null; }
        @Override public java.util.List<org.litebridge.db.spi.Row> select(org.litebridge.db.spi.query.Select select, org.litebridge.db.spi.tx.ConnectionProvider connectionProvider) { return null; }
        @Override public org.litebridge.db.spi.update.UpdateResult delete(org.litebridge.db.spi.update.Delete delete, org.litebridge.db.spi.tx.ConnectionProvider connectionProvider) { return null; }
        @Override public String toSql(org.litebridge.db.spi.query.Select select) { return null; }
        @Override public org.litebridge.db.spi.convert.TypeConverter getTypeConverter() { return null; }
    }
}
