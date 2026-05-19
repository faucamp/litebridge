package org.litebridgedb.example.h2;

import org.flywaydb.core.Flyway;
import org.litebridgedb.db.h2.H2DatabaseProvider;
import org.litebridgedb.example.common.PersistenceExample;
import org.litebridgedb.example.common.QueryExample;
import org.litebridgedb.example.common.SqlExample;
import org.litebridgedb.example.common.mapping.CommonDtoRegistration;
import org.litebridgedb.orm.Litebridge;
import org.litebridgedb.orm.tx.DefaultTransactionManager;
import org.litebridgedb.orm.tx.LitebridgeDriverManagerDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class H2Example {

    private static final Logger LOGGER = LoggerFactory.getLogger(H2Example.class);

    public static void main(String[] args) {
        // Setup H2 in-memory database
        final String url = "jdbc:h2:mem:lb;DB_CLOSE_DELAY=-1";
        final String user = "sa";
        final String password = "";
        configureDatabase(url, user, password);

        try {
            runExamples(new LitebridgeDriverManagerDataSource(url, user, password));
        } catch (Exception ex) {
            LOGGER.error("An error occurred during H2 example execution", ex);
        }
    }

    private static void runExamples(final DataSource dataSource) {
        // Initialise litebridgedb and register DTO-table mappings
        final Litebridge litebridge = new Litebridge(new H2DatabaseProvider(), new DefaultTransactionManager(dataSource));
        CommonDtoRegistration.registerPersonAndAccount(litebridge);

        new PersistenceExample(litebridge).run();
        new QueryExample(litebridge).run();
        new SqlExample(litebridge).run();
    }

    public static void configureDatabase(final String url, final String user, final String password) {
        // Configure Flyway
        final Flyway flyway = Flyway.configure()
                .dataSource(url, user, password) // Replace with your database details
                .locations("classpath:db/migration") // Specify the location of your migration scripts
                .load();

        // Run the migration
        flyway.migrate();
    }
}
