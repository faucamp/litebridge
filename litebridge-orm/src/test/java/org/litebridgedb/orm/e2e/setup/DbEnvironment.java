package org.litebridgedb.orm.e2e.setup;

import org.litebridgedb.db.spi.DatabaseProvider;
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
}