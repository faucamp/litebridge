package org.litebridge.orm.e2e.setup;

import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.orm.Litebridge;
import org.litebridge.orm.tx.LitebridgeDriverManagerDataSource;

public interface DbEnvironment {
    void start() throws Exception;
    void stop() throws Exception;
    String getJdbcUrl();
    String getUsername();
    String getPassword();
    LitebridgeDriverManagerDataSource getDataSource();
    DatabaseProvider getDatabaseProvider();
    String[] getMigrationLocations();
    String getName();

    /**
     * Allows specific database environments to override the default DTO-table mapping/registration.
     * This is needed for some database environments (e.g. SQLite) that do not support sequences etc
     */
    default DbEnvDtoTableMapper getDtoTableMapper() {
        return new DefaultDtoTableMapper();
    }
}