package org.litebridgedb.orm.e2e.setup;

import org.litebridgedb.db.spi.DatabaseProvider;
import org.litebridgedb.orm.Litebridge;
import org.litebridgedb.orm.tx.LitebridgeDriverManagerDataSource;

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