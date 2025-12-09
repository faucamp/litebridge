package org.litebridge.example.h2;

import org.flywaydb.core.Flyway;
import org.litebridge.core.LiteBridge;
import org.litebridge.db.api.DatabaseProvider;
import org.litebridge.db.h2.H2DatabaseProvider;
import org.litebridge.example.common.dto.Account;
import org.litebridge.example.common.dto.Person;
import org.litebridge.example.common.mapping.DtoTableMap;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.litebridge.core.TableSpec.t;

public class H2Example {

    public static void main(String[] args) {
        final Logger logger = configureLogging();

        final String url = "jdbc:h2:mem:lb;DB_CLOSE_DELAY=-1";
        final String user = "sa";
        final String password = "";
        configureDatabase(url, user, password);

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            final LiteBridge liteBridge = new LiteBridge(new H2DatabaseProvider(connection));
            liteBridge.register(Person.class, t("LB", "PERSON", DtoTableMap.Person));
            liteBridge.register(Account.class, t("LB", "ACCOUNT", DtoTableMap.Account));

            final Person person = liteBridge.track(new Person());
            person.setName("Alice");
            person.setSurname("Smith");
            person.setAge(20);
            person.setEyeColour("blue");

            final Account account = liteBridge.track(new Account());
            account.setName("Test account");
            account.setOwner(person);

            liteBridge.save(person);
            liteBridge.save(account);

            logger.info("Saved person ID: " + person.getId());
            logger.info("Saved account ID: " + account.getId());

            person.setEyeColour("brown");
            liteBridge.save(person);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Logger configureLogging() {
        final Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(Level.ALL);

        final ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.ALL);
        rootLogger.addHandler(consoleHandler);

        return rootLogger;
    }

    private static String configureDatabase(final String url, final String user, final String password) {
        // Configure Flyway
        final Flyway flyway = Flyway.configure()
                .dataSource(url, user, password) // Replace with your database details
                .locations("classpath:db/migration") // Specify the location of your migration scripts
                .load();

        // Run the migration
        flyway.migrate();
        return url;
    }
}
