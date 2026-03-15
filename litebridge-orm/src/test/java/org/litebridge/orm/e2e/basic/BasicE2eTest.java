package org.litebridge.orm.e2e.basic;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.tx.TransactionControl;
import org.litebridge.orm.e2e.AbstractE2eTest;
import org.litebridge.orm.e2e.basic.dto.Account;
import org.litebridge.orm.e2e.basic.dto.Person;
import org.litebridge.orm.e2e.basic.dto.PersonAccount;
import org.litebridge.orm.e2e.basic.mapping.DtoTableMap;
import org.litebridge.orm.persistence.DtoEntityMapping;
import org.litebridge.orm.persistence.EntityDtoMapper;
import org.litebridge.orm.tx.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.litebridge.orm.api.spec.FieldMapping.f;
import static org.litebridge.orm.api.spec.TableSpec.t;

class BasicE2eTest extends AbstractE2eTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicE2eTest.class);

    @Test
    @DisplayName("Select DTO and join fetch related DTOs")
    void nestedDtos_fetchRelatedDtos() throws Exception {
        // Given
        litebridge.register(Person.class, t("LB", "PERSON", DtoTableMap.Person));
        litebridge.register(Account.class, t("LB", "ACCOUNT", DtoTableMap.Account));

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

        // When
        final Account result = litebridge.select(Account.class)
                .join(Person.class).on("owner")
                .where("id").eq(person.getId())
                .oneOrThrow();

        // Then
        assertEquals(person, result.getOwner());
    }

    @Test
    @DisplayName("Select DTO without related DTOs")
    void nestedDtos_dontfetchRelatedDtos() throws Exception {
        // Given
        litebridge.register(Person.class, t("LB", "PERSON", DtoTableMap.Person));
        litebridge.register(Account.class, t("LB", "ACCOUNT", DtoTableMap.Account));

        final Person person = new Person();
        person.setName("Alice");
        person.setSurname("Smith");
        person.setAge(20);
        person.setEyeColour("blue");

        final Account account = new Account();
        account.setName("Account 1");
        account.setBalance(BigInteger.valueOf(1000));
        account.setOwner(person);

        litebridge.save(person);
        litebridge.save(account);

        // When
        final Account result = litebridge.select(Account.class)
                .where("id").eq(person.getId())
                .oneOrThrow();

        // Then
        assertNull(result.getOwner());
    }

    @Test
    @DisplayName("Nested DTOs mapped to separate tables, cascading save, no transactions (autocommit)")
    void nestedDtos_oneTablePerDto_cascadeSave_autoCommit() throws Exception {
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

        // Save DTOs ("person" will also be saved due to cascading)
        litebridge.save(account);

        // Then
        assertNotNull(person.getId(), "Person ID should be set after save");
        assertNotNull(account.getId(), "Account ID should be set after save");
        assertNotNull(person.getAccounts(), "Person should have a list of accounts");
        assertEquals(1, person.getAccounts().size(), "Person should have exactly one account");

        // When
        final Account account2 = litebridge.track(new Account());
        account2.setName("Account 2");
        account2.setBalance(BigInteger.valueOf(2000));
        account2.setOwner(person);
        litebridge.save(account2);

        person.setEyeColour("brown");
        litebridge.save(person);

        // Then
        final Person fetchedPerson = litebridge.select(Person.class)
                .join(Account.class).on("accounts")
                .where("id").eq(person.getId())
                .oneOrThrow();
        assertEquals("Alice", fetchedPerson.getName());
        assertNotNull(fetchedPerson.getAccounts());
        assertEquals(2, fetchedPerson.getAccounts().size());
        assertTrue(fetchedPerson.getAccounts().contains(account));
        assertTrue(fetchedPerson.getAccounts().contains(account2));

        final Account fetchedAccount = litebridge.select(Account.class)
                .join(Person.class).on("owner")
                .where("id").eq(account2.getId())
                .oneOrThrow();

        assertEquals("Account 2", fetchedAccount.getName());
        assertEquals(person, fetchedAccount.getOwner());
        assertNotNull(fetchedAccount.getOwner().getAccounts(), "Person should have a list of accounts");
        assertEquals(1, fetchedAccount.getOwner().getAccounts().size(), "Only 1 account should be present since we selected a single Account from the Account side");
    }

    @Test
    @DisplayName("Nested DTOs mapped to separate tables, cascading save in transaction")
    void nestedDtos_oneTablePerDto_cascadeSave_transaction() throws Exception {
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
            tx.commit();
        }

        // Then
        assertNotNull(person.getId(), "Person ID should be set after save");
        assertNotNull(account.getId(), "Account ID should be set after save");
        assertNotNull(person.getAccounts(), "Person should have a list of accounts");
        assertEquals(1, person.getAccounts().size(), "Person should have exactly one account");

        final Person fetchedPerson = litebridge.select(Person.class)
                .join(Account.class).on("accounts")
                .where("id").eq(person.getId())
                .oneOrThrow();
        assertEquals("Alice", fetchedPerson.getName());
        assertNotNull(fetchedPerson.getAccounts());
        assertEquals(2, fetchedPerson.getAccounts().size());
        assertTrue(fetchedPerson.getAccounts().contains(account));
        assertTrue(fetchedPerson.getAccounts().contains(account2));

        final Account fetchedAccount = litebridge.select(Account.class)
                .join(Person.class).on("owner")
                .where("id").eq(account2.getId())
                .oneOrThrow();

        assertEquals("Account 2", fetchedAccount.getName());
        assertEquals(person, fetchedAccount.getOwner());
        assertNotNull(fetchedAccount.getOwner().getAccounts(), "Person should have a list of accounts");
        assertEquals(1, fetchedAccount.getOwner().getAccounts().size(), "Only 1 account should be present since we selected a single Account from the Account side");
    }

    @Test
    @DisplayName("Single DTO mapped to multiple tables")
    void singleDto_multipleTables() throws Exception {
        // Create our "original"/unmapped DTO (unmapped since Litebridge expects one table per DTO)
        final PersonAccount personAccount = new PersonAccount();
        personAccount.setId(123L);
        personAccount.setName("Bob");
        personAccount.setSurname("Smith");
        personAccount.setAge(35);
        personAccount.setAccountId(456L);
        personAccount.setAccountName("Test Account");
        personAccount.setAccountBalance(new BigInteger("1000000"));

        // Register DTO-table mappings (a client using the above "PersonMapping" DTO would need
        // to create these "entities", as the query API would not make sense for multi-table DTOs)
        litebridge.register(Person.class, t("LB", "PERSON", DtoTableMap.Person));
        litebridge.register(Account.class, t("LB", "ACCOUNT", DtoTableMap.Account));

        // Create entity-DTO mapper
        final EntityDtoMapper<PersonAccount> entityDtoMapper = litebridge.entityDtoMapper(PersonAccount.class,
                List.of(new DtoEntityMapping(Person.class,
                                Map.of(
                                        f("id"), f("id"),
                                        f("name"), f("name"),
                                        f("surname"), f("surname"),
                                        f("age"), f("age")
                                )),
                        new DtoEntityMapping(Account.class,
                                Map.of(
                                        f("accountId"), f("id"),
                                        f("accountName"), f("name"),
                                        f("accountBalance"), f("balance"),
                                        f("id"), f("owner.id")
                                ))));

        // Split the multi-table DTO into two single-table DTOs and save them separately
        entityDtoMapper.entities(personAccount).forEach(litebridge::save);

        // Load the indidual entities and reconstruct the composite DTO
        final Person person = litebridge.select(Person.class).where("id").eq(personAccount.getId()).oneOrThrow();
        final Account account = litebridge.select(Account.class).where("id").eq(personAccount.getAccountId()).oneOrThrow();
        final PersonAccount result = entityDtoMapper.dto(person, account);

        // Then
        assertEquals(personAccount, result);
    }
}