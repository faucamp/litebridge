package org.litebridge.orm.e2e;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.litebridge.db.h2.H2DatabaseProvider;
import org.litebridge.orm.Litebridge;
import org.litebridge.orm.tx.DefaultTransactionManager;
import org.litebridge.orm.tx.SingleConnectionDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class AbstractE2eTest {

    private static SingleConnectionDataSource dataSource;
    protected Litebridge litebridge;

    @BeforeEach
    void beforeEach() throws SQLException {
        litebridge = resetLiteBridge();
    }

    @AfterAll
    static void afterAll() throws SQLException {
        if (dataSource != null) {
            shutdownInMemoryH2();
        }
    }

    protected DataSource dataSource() {
        return dataSource;
    }

    private Litebridge ensureLitebridge() throws SQLException {
        if (dataSource() == null) {
            dataSource = createH2DataSource();
            litebridge = new Litebridge(new H2DatabaseProvider(), dataSource, new DefaultTransactionManager(dataSource));
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
     */
    private SingleConnectionDataSource createH2DataSource() {
        final String url = "jdbc:h2:mem:lb;DB_CLOSE_DELAY=-1";
        final String user = "sa";
        final String password = "";
        runFlywayMigration(url, user, password);
        return new SingleConnectionDataSource(url, user, password);
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
        if (dataSource != null) {
            final Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            statement.execute("SHUTDOWN");
            connection.close();
            dataSource = null;
        }
    }
}