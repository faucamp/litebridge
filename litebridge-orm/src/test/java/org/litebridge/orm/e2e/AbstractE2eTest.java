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

@ExtendWith(MultiDbTestExtension.class)
public abstract class AbstractE2eTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractE2eTest.class);
    protected Litebridge litebridge;
    protected DbEnvironment dbEnv;
    private Flyway flyway;

    @BeforeEach
    void beforeEach(DbEnvironment env) throws Exception {
        this.dbEnv = env;
        this.dbEnv.start(); // If not already started

        // Init Flyway on the specific DB
        runFlywayMigration(env);

        SingleConnectionDataSource ds = dbEnv.getDataSource();

        this.litebridge = new Litebridge(
                dbEnv.getDatabaseProvider(),
                ds,
                new DefaultTransactionManager(ds)
        );
    }

    @AfterEach
    void afterEach() throws Exception {
        // Cleanup database state between tests
        flyway.clean();
        dbEnv.stop();
        dbEnv = null;
    }

    /**
     * Runs Flyway migration on the supplied database connection.
     *
     * @param env Test database environment
     */
    private void runFlywayMigration(final DbEnvironment env) {
        // Configure and run Flyway migration
        if (flyway == null) {
            flyway = Flyway.configure()
                    .dataSource(env.getJdbcUrl(), env.getUsername(), env.getPassword())
                    .locations(env.getMigrationLocations())
                    .cleanDisabled(false)
                    .load();

        }

        flyway.migrate();
    }
}