package org.litebridgedb.orm.e2e.setup;

import org.litebridgedb.db.postgres.PostgresDatabaseProvider;
import org.litebridgedb.db.spi.DatabaseProvider;
import org.litebridgedb.orm.tx.LitebridgeDriverManagerDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostgresDbEnvironment implements DbEnvironment {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostgresDbEnvironment.class);
    private final PostgresContainerManager containerManager = PostgresContainerManager.getInstance();
    private LitebridgeDriverManagerDataSource dataSource;

    @Override
    public void start() {
        containerManager.start();
        LOGGER.debug("PostgresqlDbEnvironment ready (using shared container)");
    }

    @Override
    public void stop() {
        if (dataSource != null) {
            try {
                dataSource.getConnection().close();
            } catch (Exception e) {
                LOGGER.warn("Failed to close data source connection", e);
            }
            dataSource = null;
        }
        LOGGER.debug("PostgresqlDbEnvironment data source closed (container remains running)");
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
        return new PostgresDatabaseProvider();
    }

    @Override
    public String[] getMigrationLocations() {
        return new String[]{"classpath:db/migration/postgresql"};
    }

    @Override
    public String getName() {
        return "PostgreSQL";
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

    @Override
    public DbEnvDtoTableMapper getDtoTableMapper() {
        return new PostgresDbEnvDtoTableMapper();
    }
}
