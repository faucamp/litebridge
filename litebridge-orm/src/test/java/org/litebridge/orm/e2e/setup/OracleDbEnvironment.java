package org.litebridge.orm.e2e.setup;

import org.litebridge.db.oracle.OracleDatabaseProvider;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.orm.tx.SingleConnectionDataSource;
import org.testcontainers.containers.OracleContainer;

import javax.sql.DataSource;

public class OracleDbEnvironment implements DbEnvironment {

    private final OracleContainer container = new OracleContainer("gvenzl/oracle-xe:21-slim-faststart")
            .withDatabaseName("testdb")
            .withUsername("LB")
            .withPassword("password");

    @Override
    public void start() {
        container.start();
    }

    @Override
    public void stop() {
        container.stop();
    }

    @Override
    public String getJdbcUrl() {
        return container.getJdbcUrl();
    }

    @Override
    public String getUsername() {
        return container.getUsername();
    }

    @Override
    public String getPassword() {
        return container.getPassword();
    }

    @Override
    public DatabaseProvider getDatabaseProvider() {
        return new OracleDatabaseProvider();
    }

    @Override
    public String[] getMigrationLocations() {
        return new String[]{"classpath:db/migration/common"};
    }

    @Override
    public SingleConnectionDataSource getDataSource() {
        return new SingleConnectionDataSource(container.getJdbcUrl(), container.getUsername(), container.getPassword());
    }
}