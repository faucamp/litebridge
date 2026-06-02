package org.litebridgedb.orm.e2e.annotation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import org.litebridgedb.orm.e2e.AbstractE2eTest;
import org.litebridgedb.orm.e2e.annotation.entity.Account;
import org.litebridgedb.orm.e2e.annotation.entity.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Tests using annotated entity classes instead of raw DTOs.
 * <p>
 * Also refer to org.litebridgedb.orm.e2e.manytomany.ManyToManyE2eTest.nestedEntities_fetchRelatedEntities() for many-to-many tests
 */
public class AnnotationE2eTest extends AbstractE2eTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationE2eTest.class);

    @TestTemplate
    @DisplayName("Select DTO and join fetch related DTOs")
    void entityRegistration_oneToMany() throws Exception {
        // Test entities specify the "LB" schema in the @Table annotation, so skip SQLite (no schemas) and Postgres (lowercase)
        assumeTrue(!dbEnv.getName().equals("SQLite") && !dbEnv.getName().equals("PostgreSQL"));

        // Register DTO-table mappings
        litebridge.register(Person.class);
        litebridge.register(Account.class);

        // Setup DTOs
        final Person person = new Person();
        person.setName("Alice");
        person.setSurname("Smith");
        person.setAge(20);
        person.setEyeColour("blue");

        final Account account = new Account();
        account.setName("Account 1");
        account.setBalance(BigInteger.valueOf(1000));
        account.setOwner(person);

        person.setAccounts(List.of(account));

        // Insert the person record and cascade save to linked accounts
        litebridge.save(person);
        // This save is unnecessary; it should not result in any db updates
        litebridge.save(account);

        // Retrieve the account record and associated owner
        final Account result = litebridge.select(Account.class)
                .join(Person.class).on("owner")
                .where("id").eq(person.getId())
                .oneOrThrow();

        // Then
        assertEquals(person, result.getOwner());
    }
}