package org.litebridgedb.orm.e2e;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.litebridgedb.orm.Litebridge;
import org.litebridgedb.orm.e2e.setup.DbEnvironment;
import org.litebridgedb.orm.e2e.setup.MultiDbTestExtension;
import org.litebridgedb.orm.tx.DefaultTransactionManager;
import org.litebridgedb.orm.tx.LitebridgeDriverManagerDataSource;
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

        LitebridgeDriverManagerDataSource ds = dbEnv.getDataSource();

        this.litebridge = new Litebridge(
                dbEnv.getDatabaseProvider(),
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
                    .dataSource(env.getDataSource())
                    .locations(env.getMigrationLocations())
                    .cleanDisabled(false)
                    .load();

        }

        flyway.migrate();
    }
}