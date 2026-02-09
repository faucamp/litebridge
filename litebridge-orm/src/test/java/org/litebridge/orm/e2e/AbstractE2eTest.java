package org.litebridge.orm.e2e;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.litebridge.commons.ObjectUtils;
import org.litebridge.db.h2.H2DatabaseProvider;
import org.litebridge.orm.Litebridge;
import org.litebridge.tracking.ChangeTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class AbstractE2eTest {

    private static Connection connection;
    protected Litebridge litebridge;
    protected ChangeTracker changeTracker;

    @BeforeEach
    void beforeEach() throws SQLException {
        litebridge = resetLiteBridge();
    }

    @AfterAll
    static void afterAll() throws SQLException {
        if (connection != null) {
            shutdownInMemoryH2();
        }
    }

    private Litebridge ensureLitebridge() throws SQLException {
        if (connection == null) {
            connection = createH2Connection();
            litebridge = new Litebridge(new H2DatabaseProvider(connection));
            changeTracker = ObjectUtils.getFieldValue(litebridge, "changeTracker", ChangeTracker.class);
        }

        return litebridge;
    }

    /**
     * Resets the Litebridge instance by shutting down the in-memory H2 database and ensuring a new connection.
     *
     * @return Litebridge instance
     * @throws SQLException if shutdown or connection creation fails
     */
    private Litebridge resetLiteBridge() throws SQLException {
        shutdownInMemoryH2();
        return ensureLitebridge();
    }

    /**
     * Creates an H2 in-memory database connection.
     *
     * @return H2 database connection
     * @throws SQLException if connection creation fails
     */
    private Connection createH2Connection() throws SQLException {
        final String url = "jdbc:h2:mem:lb;DB_CLOSE_DELAY=-1";
        final String user = "sa";
        final String password = "";
        runFlywayMigration(url, user, password);
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Runs Flyway migration on the supplied database connection.
     *
     * @param url      Database connection URL
     * @param user     Database user name
     * @param password Database user password
     */
    private static void runFlywayMigration(final String url, final String user, final String password) {
        // Configure and run Flyway migration
        final Flyway flyway = Flyway.configure()
                .dataSource(url, user, password)
                .locations("classpath:db/migration")
                .load();

        flyway.migrate();
    }

    /**
     * Shuts down the in-memory H2 database connection.
     *
     * @throws SQLException if shutdown fails
     */
    private static void shutdownInMemoryH2() throws SQLException {
        if (connection != null) {
            Statement statement = connection.createStatement();
            statement.execute("SHUTDOWN");
            connection.close();
            connection = null;
        }
    }
}