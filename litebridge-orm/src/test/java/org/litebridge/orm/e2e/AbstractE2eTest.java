package org.litebridge.orm.e2e;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.litebridge.orm.Litebridge;
import org.litebridge.orm.e2e.setup.DbEnvironment;
import org.litebridge.orm.e2e.setup.MultiDbTestExtension;
import org.litebridge.orm.tx.DefaultTransactionManager;
import org.litebridge.orm.tx.SingleConnectionDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

@ExtendWith(MultiDbTestExtension.class)
public abstract class AbstractE2eTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractE2eTest.class);
    protected Litebridge litebridge;
    protected DbEnvironment dbEnv;

    @BeforeEach
    void setup(DbEnvironment env) throws SQLException {
        this.dbEnv = env;
        this.dbEnv.start(); // If not already started

        // Run Flyway on the specific DB
        runFlywayMigration(env);

        SingleConnectionDataSource ds = dbEnv.getDataSource();

        this.litebridge = new Litebridge(
                dbEnv.getDatabaseProvider(),
                ds,
                new DefaultTransactionManager(ds)
        );
    }

    @AfterEach
    void tearDown() {
        try {
            dbEnv.stop();
        } catch (Exception ex) {
            LOGGER.error("Failed to stop database environment", ex);
        } finally {
            dbEnv = null;
        }
    }

    /**
     * Runs Flyway migration on the supplied database connection.
     *
     * @param url      Database connection URL
     * @param user     Database user name
     * @param password Database user password
     */
    private static void runFlywayMigration(final DbEnvironment env) {
        // Configure and run Flyway migration
        final Flyway flyway = Flyway.configure()
                .dataSource(env.getJdbcUrl(), env.getUsername(), env.getPassword())
                .locations(env.getMigrationLocations())
                .load();

        flyway.migrate();
    }
}