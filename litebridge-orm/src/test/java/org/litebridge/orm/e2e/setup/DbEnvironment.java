package org.litebridge.orm.e2e.setup;

import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.orm.tx.SingleConnectionDataSource;

public interface DbEnvironment {
    void start() throws Exception;
    void stop() throws Exception;
    String getJdbcUrl();
    String getUsername();
    String getPassword();
    SingleConnectionDataSource getDataSource();
    DatabaseProvider getDatabaseProvider();
    String[] getMigrationLocations();
    String getName();
}