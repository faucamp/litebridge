package org.litebridge.orm.e2e.basic;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.tx.TransactionException;
import org.litebridge.orm.e2e.AbstractE2eTest;
import org.litebridge.orm.e2e.basic.dto.Account;
import org.litebridge.orm.e2e.basic.dto.Person;
import org.litebridge.orm.e2e.basic.mapping.DtoTableMap;
import org.litebridge.orm.tx.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.litebridge.orm.api.spec.TableSpec.t;

class TransactionsE2eTest extends AbstractE2eTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionsE2eTest.class);

    @Test
    @SuppressWarnings("resource")
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
    @DisplayName("Transaction commit: lambda")
    void transaction_commit_lambda() throws Exception {
        // Given
        litebridge.register(Person.class, t("LB", "PERSON", DtoTableMap.Person));

        final Person person = new Person();
        person.setName("Alice");
        person.setSurname("Smith");
        person.setAge(20);
        person.setEyeColour("blue");

        // When
        litebridge.transaction().execute(() -> litebridge.save(person));

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

        boolean personIdSetDuringTransaction = person.getId() != null;
        assumeFalse(personIdSetDuringTransaction, "Person ID should net yet be set");

        // When
        try (Transaction tx = litebridge.transaction().begin()) {
            litebridge.save(person);
            personIdSetDuringTransaction = person.getId() != null;
            tx.rollback();
        }

        // Then
        final int recordCount = litebridge.select("PERSON_ID").from("LB", "PERSON").list().size();
        assertEquals(0, recordCount, "Should have no records in the database");
        assertTrue(personIdSetDuringTransaction, "Person ID should be set after save() while still in transaction");
        assertNull(person.getId(), "Person ID should not be set after transaction rollback");
    }

    @Test
    @DisplayName("Transaction explicit rollback: rollback DTO collections")
    void transaction_explicitRollback_oneToMany() throws Exception {
        // Register DTO-table mappings
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
        account.setBalance(BigInteger.valueOf(1000));
        account.setOwner(person);

        final Account account2 = litebridge.track(new Account());
        account2.setName("Account 2");
        account2.setBalance(BigInteger.valueOf(2000));
        account2.setOwner(person);

        try (Transaction tx = litebridge.transaction().begin()) {
            // Save DTOs ("person" will also be saved due to cascading)
            litebridge.save(account);
            litebridge.save(account2);
            person.setEyeColour("brown");
            litebridge.save(person);
            tx.rollback();
        }

        // Then
        final int personCount = litebridge.select("PERSON_ID").from("LB", "PERSON").list().size();
        assertEquals(0, personCount, "Should have no PERSON records in the database");
        final int accountCount = litebridge.select("ACCOUNT_ID").from("LB", "ACCOUNT").list().size();
        assertEquals(0, accountCount, "Should have no ACCOUNT records in the database");
        assertNull(person.getId(), "Person ID should not be set after rollback");
        assertNull(account.getId(), "Account ID should not be set after rollback");
        assertNull(person.getAccounts(), "Person accounts not restored to null after rollback");
    }

    @Test
    @DisplayName("Transaction rollback on exception: lambda")
    void transaction_rollback_lambda() throws Exception {
        // Given
        litebridge.register(Person.class, t("LB", "PERSON", DtoTableMap.Person));

        final Person person = new Person();
        person.setSurname("NoFirstName");
        boolean exceptionThrown = false;

        // When
        try {
            litebridge.transaction().execute(() -> litebridge.save(person));
        } catch (TransactionException ex) {
            exceptionThrown = true;
            LOGGER.info("Caught exception: {}", ex.getMessage(), ex);
        }

        // Then
        assertTrue(exceptionThrown, "Exception should be thrown");
        final int recordCount = litebridge.select("PERSON_ID").from("LB", "PERSON").list().size();
        assertEquals(0, recordCount, "Should have no records in the database");
        assertNull(person.getId(), "Person ID should not be set after transaction rollback");
    }
}