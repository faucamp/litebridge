package org.litebridge.orm.e2e.setup;

import org.litebridge.db.oracle.OracleDatabaseProvider;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.orm.tx.LitebridgeDriverManagerDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OracleDbEnvironment implements DbEnvironment {

    private static final Logger LOGGER = LoggerFactory.getLogger(OracleDbEnvironment.class);
    private final OracleContainerManager containerManager = OracleContainerManager.getInstance();
    private LitebridgeDriverManagerDataSource dataSource;

    @Override
    public void start() {
        // Start the shared container (only happens once)
        containerManager.start();
        LOGGER.debug("OracleDbEnvironment ready (using shared container)");
    }

    @Override
    public void stop() {
        // Clean up the data source, but keep the container running
        if (dataSource != null) {
            try {
                dataSource.getConnection().close();
            } catch (Exception e) {
                LOGGER.warn("Failed to close data source connection", e);
            }
            dataSource = null;
        }
        LOGGER.debug("OracleDbEnvironment data source closed (container remains running)");
    }

    @Override
    public String getJdbcUrl() {
        return containerManager.getContainer().getJdbcUrl();
    }

    @Override
    public String getUsername() {
        return containerManager.getContainer().getUsername();
    }

    @Override
    public String getPassword() {
        return containerManager.getContainer().getPassword();
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
    public String getName() {
        return "Oracle";
    }

    @Override
    public LitebridgeDriverManagerDataSource getDataSource() {
        if (dataSource == null) {
            dataSource = new LitebridgeDriverManagerDataSource(
                    containerManager.getContainer().getJdbcUrl(),
                    containerManager.getContainer().getUsername(),
                    containerManager.getContainer().getPassword()
            );
        }
        return dataSource;
    }
}