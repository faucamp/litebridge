package org.litebridgedb.example.maven;

import org.flywaydb.core.Flyway;
import org.litebridge.example.maven.entity.Account;
import org.litebridge.example.maven.entity.Person;
import org.litebridge.example.maven.metamodel.AccountMeta;
import org.litebridge.example.maven.metamodel.PersonMeta;
import org.litebridgedb.db.h2.H2DatabaseProvider;
import org.litebridgedb.orm.Litebridge;
import org.litebridgedb.orm.support.EntityScanner;
import org.litebridgedb.orm.tx.DefaultTransactionManager;
import org.litebridgedb.orm.tx.LitebridgeDriverManagerDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.List;

public class MavenPluginExample {

    private static final Logger LOGGER = LoggerFactory.getLogger(MavenPluginExample.class);

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

        // Register the reverse-engineered entities
        final Class<?>[] entities = new EntityScanner().scanBasePackage("org.litebridge.example.maven.entity");

        litebridge.register(entities);

        // Save and retrieve some entities
        final Person person = new Person();
        person.setFirstName("John");
        person.setSurname("Doe");
        person.setAge(45);
        person.setEyeColour("brown");

        final Account account = new Account();
        account.setAccountName("John's account");
        account.setOwner(person);

        litebridge.save(account);

        final Person retrievedPerson = litebridge.select(Person.class)
                .join(Account.class).on(PersonMeta.accounts)
                .where(AccountMeta.accountName).like("%account")
                .and(PersonMeta.surname.upper()).eq("DOE")
                .oneOrThrow();

        LOGGER.info("Retrieved person: {}", retrievedPerson);
    }

    public static void configureDatabase(final String url, final String user, final String password) {
        // Configure Flyway
        final Flyway flyway = Flyway.configure()
                .dataSource(url, user, password)
                .locations("classpath:db/migration")
                .load();

        // Run the migration
        flyway.migrate();
    }
}
