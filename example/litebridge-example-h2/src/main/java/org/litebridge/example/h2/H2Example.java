package org.litebridge.example.h2;

import org.flywaydb.core.Flyway;
import org.litebridge.orm.Litebridge;
import org.litebridge.db.h2.H2DatabaseProvider;
import org.litebridge.example.common.dto.Account;
import org.litebridge.example.common.dto.Person;
import org.litebridge.example.common.mapping.DtoTableMap;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Comparator;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.litebridge.orm.TableSpec.t;

public class H2Example {

    public static void main(String[] args) {
        final Logger logger = configureLogging();

        final String url = "jdbc:h2:mem:lb;DB_CLOSE_DELAY=-1";
        final String user = "sa";
        final String password = "";
        configureDatabase(url, user, password);

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            // Initialise litebridge and register DTO-table mappings
            final Litebridge litebridge = new Litebridge(new H2DatabaseProvider(connection));
            litebridge.register(Person.class, t("LB", "PERSON", DtoTableMap.Person));
            litebridge.register(Account.class, t("LB", "ACCOUNT", DtoTableMap.Account));

            // Create DTOs and enable change tracking
            final Person person = litebridge.track(new Person());
            person.setName("Alice");
            person.setSurname("Smith");
            person.setAge(20);
            person.setEyeColour("blue");

            final Account account = litebridge.track(new Account());
            account.setName("Test account");
            account.setOwner(person);

            // Save DTOs ("person" will also be saved due to cascading)
            litebridge.save(account);
            logger.info("Saved person ID: " + person.getId());
            logger.info("Saved account ID: " + account.getId());

            // Update a single field of a tracked DTO and update the database accordingly
            person.setEyeColour("brown");
            litebridge.save(person);

            // Retrieve all persons and return a List
            final List<Person> persons = litebridge.select(Person.class).getAll();
            logger.info("All persons (list): " + persons);

            // Retrieve oldest person with criteria using a Stream
            litebridge.select(Person.class)
                    .where("age").gte(18)
                    .stream()
                    .max(Comparator.comparing(Person::getAge))
                    .ifPresent(p -> logger.info("Oldest person: " + p));

            // Retrieve a single person with criteria
            final Person alice = litebridge.select(Person.class)
                    .where("name").eq("Alice")
                    .and("surname").eq("Smith")
                    .get();

            logger.info("Retrieved person: " + alice);
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
