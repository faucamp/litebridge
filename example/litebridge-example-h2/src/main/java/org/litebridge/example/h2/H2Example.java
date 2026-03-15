package org.litebridge.example.h2;

import org.flywaydb.core.Flyway;
import org.litebridge.db.h2.H2DatabaseProvider;
import org.litebridge.example.common.PersistenceExample;
import org.litebridge.example.common.QueryExample;
import org.litebridge.example.common.SqlExample;
import org.litebridge.example.common.TypeSafeExample;
import org.litebridge.example.common.dto.Account;
import org.litebridge.example.common.dto.Person;
import org.litebridge.example.common.mapping.DtoTableMap;
import org.litebridge.orm.Litebridge;
import org.litebridge.orm.tx.DefaultTransactionManager;
import org.litebridge.orm.tx.SingleConnectionDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.litebridge.orm.api.spec.TableSpec.t;

public class H2Example {

    private static final Logger LOGGER = LoggerFactory.getLogger(H2Example.class);

    public static void main(String[] args) {
        // Setup H2 in-memory database
        final String url = "jdbc:h2:mem:lb;DB_CLOSE_DELAY=-1";
        final String user = "sa";
        final String password = "";
        configureDatabase(url, user, password);

        try {
            runExamples(new SingleConnectionDataSource(url, user, password));
        } catch (Exception ex) {
            LOGGER.error("An error occurred during H2 example execution", ex);
        }
    }

    private static void runExamples(final DataSource dataSource) throws SQLException {
        // Initialise litebridge and register DTO-table mappings
        final Litebridge litebridge = new Litebridge(new H2DatabaseProvider(), dataSource, new DefaultTransactionManager(dataSource));
        litebridge.register(Person.class, t("LB", "PERSON", DtoTableMap.Person));
        litebridge.register(Account.class, t("LB", "ACCOUNT", DtoTableMap.Account));

        new PersistenceExample(litebridge).run();
        new QueryExample(litebridge).run();
        new SqlExample(litebridge).run();
        new TypeSafeExample(litebridge).run();
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
