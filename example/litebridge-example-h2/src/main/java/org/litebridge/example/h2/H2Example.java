package org.litebridge.example.h2;

import org.flywaydb.core.Flyway;
import org.litebridge.db.h2.H2DatabaseProvider;
import org.litebridge.example.common.dto.Account;
import org.litebridge.example.common.dto.Person;
import org.litebridge.example.common.mapping.DtoTableMap;
import org.litebridge.orm.Litebridge;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Comparator;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
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
            final List<Person> persons = litebridge.select(Person.class).list();
            logger.info("All persons (list): " + persons);

            // Retrieve a single person with criteria
            final Person alice = litebridge.select(Person.class)
                    .where("name").eq("Alice")
                    .and("surname").eq("Smith")
                    .oneOrNull();
            logger.info("Retrieved person (nullable): " + alice);

            // Retrieve a single person with criteria using an Optional, and log it
            litebridge.select(Person.class)
                    .where("name").eq("Alice")
                    .and("surname").eq("Smith")
                    .one()
                    .ifPresent(p -> logger.info("Retrieved person (Optional): " + p));

            // Retrieve and log the first person found
            litebridge.select(Person.class).first().ifPresent(p -> logger.info("First person: " + p));

            // Retrieve oldest adult person with criteria using a Stream
            litebridge.select(Person.class)
                    .where("age").gte(18)
                    .stream()
                    .max(Comparator.comparing(Person::getAge))
                    .ifPresent(p -> logger.info("Oldest person: " + p));

            // Retrieve and log Persons that have an eye colour set
            litebridge.select(Person.class)
                    .where("eyeColour").isNotNull()
                    .stream()
                    .forEach(p -> logger.info("Person with eye colour (isNotNull): " + p));

            // Retrieve and log Persons that do not have an eye colour set, using eq(null) instead of isNull()
            litebridge.select(Person.class)
                    .where("eyeColour").eq(null)
                    .stream()
                    .forEach(p -> logger.info("Person without eye colour (eq): " + p));

            // Retrieve a person's details using a lower-level SQL query
            litebridge.select("FIRST_NAME", "SURNAME", "AGE").from("LB", "PERSON")
                    .where("AGE").gt(18)
                    .and("AGE").lt(25)
                    .stream()
                    .forEach(p -> logger.info("SQL result: Selected data for PERSON record: " + p));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Logger configureLogging() {
        final Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(Level.ALL);

        // Remove default handlers to prevent duplicate output
        for (Handler handler : rootLogger.getHandlers()) {
            rootLogger.removeHandler(handler);
        }

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
