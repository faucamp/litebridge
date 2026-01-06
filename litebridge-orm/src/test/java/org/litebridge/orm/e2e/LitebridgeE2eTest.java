package org.litebridge.orm.e2e;

import org.flywaydb.core.Flyway;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.litebridge.commons.ClassUtils;
import org.litebridge.db.h2.H2DatabaseProvider;
import org.litebridge.orm.Litebridge;
import org.litebridge.orm.e2e.dto.Account;
import org.litebridge.orm.e2e.dto.Person;
import org.litebridge.orm.e2e.mapping.DtoTableMap;
import org.litebridge.tracking.ChangeTracker;
import org.litebridge.tracking.TrackedDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.litebridge.orm.api.spec.TableSpec.t;

class LitebridgeE2eTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(LitebridgeE2eTest.class);
    private static Connection connection;
    private Litebridge litebridge;
    private ChangeTracker changeTracker;

    @BeforeEach
    void beforeEach() throws SQLException {
        litebridge = resetLiteBridge();
    }

    @AfterAll
    static void afterAll() throws SQLException {
        if (connection != null) {
            connection.close();
            connection = null;
        }
    }

    @Test
    void register() {
    }

    @Test
    void track() {
    }

    @Test
    void save() throws Exception {
        // Initialise litebridge and register DTO-table mappings
        litebridge.register(Person.class, t("LB", "PERSON", DtoTableMap.Person));
        litebridge.register(Account.class, t("LB", "ACCOUNT", DtoTableMap.Account));

        // Create DTOs and enable change tracking
        final Person person = litebridge.track(new Person());
        person.setName("Alice");
        person.setSurname("Smith");
        person.setAge(20);
        person.setEyeColour("blue");

        final Account account = litebridge.track(new Account());
        account.setName("Account 1");
        account.setOwner(person);

        // Save DTOs ("person" will also be saved due to cascading)
        litebridge.save(account);

        // Then
        assertNotNull(person.getId(), "Person ID should be set after save");
        final TrackedDto<Person> personTrackedDto = changeTracker.getTrackedDto(person);
        //assertTrue(personTrackedDto.changedFields().isEmpty(), "No fields should be changed after save");
        assertNotNull(account.getId(), "Account ID should be set after save");
        final TrackedDto<Account> accountTrackedDto = changeTracker.getTrackedDto(account);
        //assertTrue(accountTrackedDto.changedFields().isEmpty(), "No fields should be changed after save");

        // When
        final Account account2 = litebridge.track(new Account());
        account2.setName("Account 2");
        account2.setOwner(person);
        litebridge.save(account2);

        person.setEyeColour("brown");
        litebridge.save(person);
    }

    private Litebridge ensureLitebridge() throws SQLException {
        if (connection == null) {
            connection = createH2Connection();
            litebridge = new Litebridge(new H2DatabaseProvider(connection));
            changeTracker = (ChangeTracker) reflectFieldValue(litebridge, "changeTracker");
        }

        return litebridge;
    }

    private Litebridge resetLiteBridge() throws SQLException {
        if (connection != null) {
            connection.close();
            connection = null;
        }

        return ensureLitebridge();
    }

    private Connection createH2Connection() throws SQLException {
        // Setup H2 in-memory database
        final String url = "jdbc:h2:mem:lb;DB_CLOSE_DELAY=-1";
        final String user = "sa";
        final String password = "";
        configureDatabase(url, user, password);
        return DriverManager.getConnection(url, user, password);
    }

    private static void configureDatabase(final String url, final String user, final String password) {
        // Configure Flyway
        final Flyway flyway = Flyway.configure()
                .dataSource(url, user, password) // Replace with your database details
                .locations("classpath:db/migration") // Specify the location of your migration scripts
                .load();

        // Run the migration
        flyway.migrate();
    }

    private static @Nullable Object reflectFieldValue(final Object obj, final String fieldName) {
        try {
            final Field field = ClassUtils.getField(obj.getClass(), fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (IllegalAccessException ex) {
            throw new IllegalArgumentException("Failed to access field '" + fieldName + "' on object of type '" + obj.getClass().getName() + "'", ex);
        }
    }
}