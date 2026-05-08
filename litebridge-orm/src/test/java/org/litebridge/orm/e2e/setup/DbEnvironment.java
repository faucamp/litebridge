package org.litebridge.orm.e2e.setup;

import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.orm.tx.SingleConnectionDataSource;

import javax.sql.DataSource;

public interface DbEnvironment {
    void start();
    void stop() throws Exception;
    String getJdbcUrl();
    String getUsername();
    String getPassword();
    SingleConnectionDataSource getDataSource();
    DatabaseProvider getDatabaseProvider();
    String[] getMigrationLocations();
}