package org.litebridge.example.spring.config;

import org.litebridge.db.h2.H2DatabaseProvider;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.example.common.mapping.CommonDtoRegistration;
import org.litebridge.orm.Litebridge;
import org.litebridge.orm.api.select.SelectApi;
import org.litebridge.spring.LitebridgeEntityScanner;
import org.litebridge.spring.LitebridgeTransactionManager;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class LitebridgeConfig {

    @Bean
    public LitebridgeTransactionManager litebridgeTransactionManager(final DataSource dataSource) {
        return new LitebridgeTransactionManager(dataSource);
    }

    @Bean
    @DependsOnDatabaseInitialization
    public SelectApi litebridge(final LitebridgeTransactionManager transactionManager) {
        final DatabaseProvider databaseProvider = new H2DatabaseProvider();
        final Litebridge litebridge = new Litebridge(databaseProvider, transactionManager);

        // Register DTO-table mappings
        try {
            // Programmatic DTO registration
            CommonDtoRegistration.registerPersonAndAccount(litebridge);
            // Entity registration via package scanning
            final Class<?>[] entities = new LitebridgeEntityScanner().scanBasePackage("org.litebridge.example.common.entity");
            litebridge.register(entities);
        } catch (final Exception ex) {
            throw new IllegalStateException("Failed to register DTO-table mappings", ex);
        }

        return litebridge;
    }

}
