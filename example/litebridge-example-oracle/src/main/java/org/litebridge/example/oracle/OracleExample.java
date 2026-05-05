package org.litebridge.example.oracle;

import org.flywaydb.core.Flyway;
import org.litebridge.db.oracle.OracleDatabaseProvider;
import org.litebridge.example.common.PersistenceExample;
import org.litebridge.example.common.QueryExample;
import org.litebridge.example.common.SqlExample;
import org.litebridge.example.common.TypeSafeExample;
import org.litebridge.example.common.mapping.CommonDtoRegistration;
import org.litebridge.orm.Litebridge;
import org.litebridge.orm.tx.DefaultTransactionManager;
import org.litebridge.orm.tx.SingleConnectionDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Example using Oracle XE.
 * <p>
 * Steps:
 * docker pull gvenzl/oracle-xe
 * docker run -d -p 1521:1521 -e ORACLE_PASSWORD=password gvenzl/oracle-xe
 */
public class OracleExample {

    private static final Logger LOGGER = LoggerFactory.getLogger(OracleExample.class);

    public static void main(String[] args) {
        // Connect to Oracle XE
        final String url = "jdbc:oracle:thin:@//localhost:1521/XEPDB1";

        final String adminUser = "system";
        final String adminPassword = "password";

        final String litebridgeUser = "LB";
        final String litebridgePassword = "password";

        configureDatabase(url, adminUser, adminPassword, litebridgeUser, litebridgePassword);

        try {
            runExamples(new SingleConnectionDataSource(url, litebridgeUser, litebridgePassword));
        } catch (Exception ex) {
            LOGGER.error("An error occurred during Oracle example execution", ex);
        }
    }

    private static void runExamples(final DataSource dataSource) throws SQLException {
        // Initialise litebridge and register DTO-table mappings
        final Litebridge litebridge = new Litebridge(new OracleDatabaseProvider(), dataSource, new DefaultTransactionManager(dataSource));
        CommonDtoRegistration.registerPersonAndAccount(litebridge);

        new PersistenceExample(litebridge).run();
        new QueryExample(litebridge).run();
        new SqlExample(litebridge).run();
        new TypeSafeExample(litebridge).run();
    }

    public static void configureDatabase(
            final String url,
            final String adminUser,
            final String adminPassword,
            final String litebridgeUser,
            final String liteBridgePassword
    ) {
        createApplicationUserIfMissing(url, adminUser, adminPassword, litebridgeUser, liteBridgePassword);

        // Configure Flyway
        final Flyway flyway = Flyway.configure()
                .baselineOnMigrate(true)
                .dataSource(url, litebridgeUser, liteBridgePassword)
                .locations("classpath:db/migration")
                .load();

        // Run the migration
        flyway.migrate();
    }

    private static void createApplicationUserIfMissing(
            final String url,
            final String adminUser,
            final String adminPassword,
            final String appUser,
            final String appPassword
    ) {
        try (
                final Connection connection = new SingleConnectionDataSource(url, adminUser, adminPassword).getConnection();
                final Statement statement = connection.createStatement()
        ) {
            statement.execute("""
                    DECLARE
                        user_count NUMBER;
                    BEGIN
                        SELECT COUNT(*)
                        INTO user_count
                        FROM ALL_USERS
                        WHERE USERNAME = UPPER('%s');
                    
                        IF user_count = 0 THEN
                            EXECUTE IMMEDIATE 'CREATE USER %s IDENTIFIED BY "%s"';
                            EXECUTE IMMEDIATE 'GRANT CREATE SESSION TO %s';
                            EXECUTE IMMEDIATE 'GRANT CREATE TABLE TO %s';
                            EXECUTE IMMEDIATE 'GRANT CREATE SEQUENCE TO %s';
                            EXECUTE IMMEDIATE 'GRANT CREATE VIEW TO %s';
                            EXECUTE IMMEDIATE 'GRANT CREATE PROCEDURE TO %s';
                            EXECUTE IMMEDIATE 'ALTER USER %s QUOTA UNLIMITED ON USERS';
                        END IF;
                    END;
                    """.formatted(
                    appUser,
                    appUser,
                    appPassword,
                    appUser,
                    appUser,
                    appUser,
                    appUser,
                    appUser,
                    appUser
            ));
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to create or configure Oracle application user " + appUser, ex);
        }
    }
}
