package org.litebridge.orm.e2e.basic;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.litebridge.orm.e2e.AbstractE2eTest;
import org.litebridge.orm.e2e.basic.dto.Person;
import org.litebridge.orm.e2e.basic.mapping.DtoTableMap;
import org.litebridge.orm.tx.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.litebridge.orm.api.spec.TableSpec.t;

class TransactionsE2eTest extends AbstractE2eTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionsE2eTest.class);

    @Test
    @DisplayName("Transaction commit: manual control")
    void transaction_commit_manual() throws Exception {
        // Given
        litebridge.register(Person.class, t("LB", "PERSON", DtoTableMap.Person));

        final Person person = new Person();
        person.setName("Alice");
        person.setSurname("Smith");
        person.setAge(20);
        person.setEyeColour("blue");

        // When
        litebridge.transaction().begin();
        litebridge.save(person);
        litebridge.transaction().commit();

        // Then
        final int recordCount = litebridge.select("PERSON_ID").from("LB", "PERSON").list().size();
        assertEquals(1, recordCount, "Should have exactly one record in the database");
        assertNotNull(person.getId(), "Person ID should be set after transaction commit");
    }

    @Test
    @DisplayName("Transaction commit: try-with-resources")
    void transaction_commit_tryWithResources() throws Exception {
        // Given
        litebridge.register(Person.class, t("LB", "PERSON", DtoTableMap.Person));

        final Person person = new Person();
        person.setName("Alice");
        person.setSurname("Smith");
        person.setAge(20);
        person.setEyeColour("blue");

        // When
        try (Transaction tx = litebridge.transaction().begin()) {
            litebridge.save(person);
            tx.commit();
        }

        // Then
        final int recordCount = litebridge.select("PERSON_ID").from("LB", "PERSON").list().size();
        assertEquals(1, recordCount, "Should have exactly one record in the database");
        assertNotNull(person.getId(), "Person ID should be set after transaction commit");
    }

    @Test
    @DisplayName("Transaction rollback on exception: manual control")
    void transaction_rollback_manual() throws Exception {
        // Given
        litebridge.register(Person.class, t("LB", "PERSON", DtoTableMap.Person));

        final Person person = new Person();
        person.setSurname("NoFirstName");
        boolean exceptionThrown = false;

        // When
        litebridge.transaction().begin();

        try {
            litebridge.save(person);
            litebridge.transaction().commit();
        } catch (Exception ex) {
            exceptionThrown = true;
            LOGGER.info("Caught exception: {}", ex.getMessage(), ex);
            litebridge.transaction().rollback();
        }

        // Then
        assertTrue(exceptionThrown, "Exception should be thrown");
        final int recordCount = litebridge.select("PERSON_ID").from("LB", "PERSON").list().size();
        assertEquals(0, recordCount, "Should have no records in the database");
        assertNull(person.getId(), "Person ID should not be set after transaction rollback");
    }

    @Test
    @DisplayName("Transaction rollback on exception: try-with-resources")
    void transaction_rollback_tryWithResources() throws Exception {
        // Given
        litebridge.register(Person.class, t("LB", "PERSON", DtoTableMap.Person));

        final Person person = new Person();
        person.setSurname("NoFirstName");
        boolean exceptionThrown = false;

        // When
        try (Transaction tx = litebridge.transaction().begin()) {
            litebridge.save(person);
        } catch (Exception ex) {
            exceptionThrown = true;
            LOGGER.info("Caught exception: {}", ex.getMessage(), ex);
        }

        // Then
        assertTrue(exceptionThrown, "Exception should be thrown");
        final int recordCount = litebridge.select("PERSON_ID").from("LB", "PERSON").list().size();
        assertEquals(0, recordCount, "Should have no records in the database");
        assertNull(person.getId(), "Person ID should not be set after transaction rollback");
    }

    @Test
    @DisplayName("Transaction explicit rollback: try-with-resources")
    void transaction_explicitRollback_tryWithResources() throws Exception {
        // Given
        litebridge.register(Person.class, t("LB", "PERSON", DtoTableMap.Person));

        final Person person = new Person();
        person.setName("Alice");
        person.setSurname("Smith");
        person.setAge(20);
        person.setEyeColour("blue");

        // When
        try (Transaction tx = litebridge.transaction().begin()) {
            litebridge.save(person);
            tx.rollback();
        }

        // Then
        final int recordCount = litebridge.select("PERSON_ID").from("LB", "PERSON").list().size();
        assertEquals(0, recordCount, "Should have no records in the database");
        assertNull(person.getId(), "Person ID should not be set after transaction rollback");
    }
}