package org.litebridge.orm.e2e.setup;

import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.LitebridgeOverrideDatabaseProvider;
import org.litebridge.orm.LitebridgeCore;
import org.litebridge.orm.Litebridge;
import org.litebridge.orm.LitebridgeBuilder;
import org.litebridge.orm.config.LitebridgeConfig;
import org.litebridge.orm.tx.LitebridgeDriverManagerDataSource;

import javax.sql.DataSource;
import java.lang.invoke.MethodHandles;

public interface DbEnvironment<LB extends LitebridgeCore> {

    void start() throws Exception;

    void stop() throws Exception;

    LitebridgeDriverManagerDataSource getDataSource();

    DatabaseProvider getDatabaseProvider();

    String[] getMigrationLocations();

    String getName();

    default LB createLitebridge(LitebridgeConfig litebridgeConfig, DataSource dataSource) {
        final DatabaseProvider databaseProvider = getDatabaseProvider();
        final LitebridgeBuilder<LB> litebridgeBuilder;

        if (databaseProvider instanceof LitebridgeOverrideDatabaseProvider litebridgeOverrideDatabaseProvider) {
            litebridgeBuilder = Litebridge.withDatabase(litebridgeOverrideDatabaseProvider, dataSource);
        } else {
            litebridgeBuilder = (LitebridgeBuilder<LB>) Litebridge.withDatabase(getDatabaseProvider(), dataSource);
        }

        return (LB) litebridgeBuilder.withConfig(litebridgeConfig)
                .withLookup(MethodHandles.lookup())
                .build();
    }

    /**
     * Allows specific database environments to override the default DTO-table mapping/registration.
     * This is needed for some database environments (e.g. SQLite) that do not support sequences etc
     */
    default DbEnvDtoTableMapper getDtoTableMapper() {
        return new DefaultDtoTableMapper();
    }
}