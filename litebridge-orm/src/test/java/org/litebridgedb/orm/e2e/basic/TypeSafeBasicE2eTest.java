package org.litebridgedb.orm.e2e.basic;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import org.litebridgedb.orm.e2e.AbstractE2eTest;
import org.litebridgedb.orm.e2e.basic.dto.Account;
import org.litebridgedb.orm.e2e.basic.dto.Person;
import org.litebridgedb.orm.e2e.basic.dto.PersonAccount;
import org.litebridgedb.orm.e2e.basic.mapping.AccountMapping;
import org.litebridgedb.orm.e2e.basic.mapping.PersonMapping;
import org.litebridgedb.orm.e2e.setup.DbEnvDtoTableMapper;
import org.litebridgedb.orm.persistence.DtoEntityMapping;
import org.litebridgedb.orm.persistence.EntityDtoMapper;
import org.litebridgedb.orm.tx.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.litebridgedb.orm.api.spec.FieldMapping.f;

public class TypeSafeBasicE2eTest extends AbstractE2eTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TypeSafeBasicE2eTest.class);

    @TestTemplate
    @DisplayName("Type safe: Select DTO and join fetch related DTOs")
    void nestedDtos_fetchRelatedDtos(final DbEnvDtoTableMapper tableMapper) throws Exception {

        // Test mappings specify uppercase schema/table/column names, so skip Postgres
        assumeTrue(!dbEnv.getName().equals("PostgreSQL"));

        // Register DTO-table mappings
        tableMapper.registerPersonAndAccountDtoTableMappings(litebridge, true);

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
                .join(Person.class).on(AccountMapping.owner)
                .where(PersonMapping.id).eq(person.getId())
                .oneOrThrow();

        // Then
        assertEquals(person, result.getOwner());
    }

    @TestTemplate
    @DisplayName("Type safe: Select DTO without related DTOs")
    void nestedDtos_dontfetchRelatedDtos(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Test mappings specify uppercase schema/table/column names, so skip Postgres
        assumeTrue(!dbEnv.getName().equals("PostgreSQL"));

        // Register DTO-table mappings
        tableMapper.registerPersonAndAccountDtoTableMappings(litebridge, true);

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
                .where(PersonMapping.id).eq(person.getId())
                .oneOrThrow();

        // Then
        assertNull(result.getOwner());
    }

    @TestTemplate
    @DisplayName("Type safe: Nested DTOs mapped to separate tables, cascading save, no transactions (autocommit)")
    void nestedDtos_oneTablePerDto_cascadeSave_autoCommit(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Test mappings specify uppercase schema/table/column names, so skip Postgres
        assumeTrue(!dbEnv.getName().equals("PostgreSQL"));

        // Register DTO-table mappings
        tableMapper.registerPersonAndAccountDtoTableMappings(litebridge, true);

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
                .join(Account.class).on(PersonMapping.accounts)
                .where(PersonMapping.id).eq(person.getId())
                .oneOrThrow();
        assertEquals("Alice", fetchedPerson.getName());
        assertNotNull(fetchedPerson.getAccounts());
        assertEquals(2, fetchedPerson.getAccounts().size());
        assertTrue(fetchedPerson.getAccounts().contains(account));
        assertTrue(fetchedPerson.getAccounts().contains(account2));

        final Account fetchedAccount = litebridge.select(Account.class)
                .join(Person.class).on(AccountMapping.owner)
                .where(AccountMapping.id).eq(account2.getId())
                .oneOrThrow();

        assertEquals("Account 2", fetchedAccount.getName());
        assertEquals(person, fetchedAccount.getOwner());
        assertNotNull(fetchedAccount.getOwner().getAccounts(), "Person should have a list of accounts");
        assertEquals(1, fetchedAccount.getOwner().getAccounts().size(), "Only 1 account should be present since we selected a single Account from the Account side");
    }

    @TestTemplate
    @DisplayName("Type safe: Nested DTOs mapped to separate tables, cascading save in transaction")
    void nestedDtos_oneTablePerDto_cascadeSave_transaction(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Test mappings specify uppercase schema/table/column names, so skip Postgres
        assumeTrue(!dbEnv.getName().equals("PostgreSQL"));

        // Register DTO-table mappings
        tableMapper.registerPersonAndAccountDtoTableMappings(litebridge, true);

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
        assertEquals(2, person.getAccounts().size(), "Person should have exactly two accounts");

        final Person fetchedPerson = litebridge.select(Person.class)
                .join(Account.class).on(PersonMapping.accounts)
                .where(AccountMapping.id).eq(person.getId())
                .oneOrThrow();
        assertEquals("Alice", fetchedPerson.getName());
        assertNotNull(fetchedPerson.getAccounts());
        assertEquals(2, fetchedPerson.getAccounts().size());
        assertTrue(fetchedPerson.getAccounts().contains(account));
        assertTrue(fetchedPerson.getAccounts().contains(account2));

        final Account fetchedAccount = litebridge.select(Account.class)
                .join(Person.class).on(AccountMapping.owner)
                .where(AccountMapping.id).eq(account2.getId())
                .oneOrThrow();

        assertEquals("Account 2", fetchedAccount.getName());
        assertEquals(person, fetchedAccount.getOwner());
        assertNotNull(fetchedAccount.getOwner().getAccounts(), "Person should have a list of accounts");
        assertEquals(1, fetchedAccount.getOwner().getAccounts().size(), "Only 1 account should be present since we selected a single Account from the Account side");
    }

    @TestTemplate
    @DisplayName("Type safe: Single DTO mapped to multiple tables")
    void singleDto_multipleTables(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Test mappings specify uppercase schema/table/column names, so skip Postgres
        assumeTrue(!dbEnv.getName().equals("PostgreSQL"));

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
        tableMapper.registerPersonAndAccountDtoTableMappings(litebridge, true);

        // Create entity-DTO mapper
        //TODO: make this type safe
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
        final Person person = litebridge.select(Person.class).where(PersonMapping.id).eq(personAccount.getId()).oneOrThrow();
        final Account account = litebridge.select(Account.class).where(AccountMapping.id).eq(personAccount.getAccountId()).oneOrThrow();
        final PersonAccount result = entityDtoMapper.dto(person, account);

        // Then
        assertEquals(personAccount, result);
    }

    @TestTemplate
    @DisplayName("Type safe: Delete DTOs, no transactions (autocommit)")
    void delete_autoCommit(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Test mappings specify uppercase schema/table/column names, so skip Postgres
        assumeTrue(!dbEnv.getName().equals("PostgreSQL"));

        // Register DTO-table mappings
        tableMapper.registerPersonAndAccountDtoTableMappings(litebridge, true);

        // Create DTOs and enable change tracking
        final Person person1 = new Person();
        person1.setName("Alice");
        person1.setSurname("Smith");
        person1.setAge(20);
        person1.setEyeColour("blue");

        final Person person2 = new Person();
        person2.setName("Bob");
        person2.setSurname("Jones");
        person2.setAge(22);
        person2.setEyeColour("brown");

        final Person person3 = new Person();
        person3.setName("Frank");
        person3.setSurname("Davies");
        person3.setAge(45);
        person3.setEyeColour("brown");

        final Person person4 = new Person();
        person4.setName("John");
        person4.setSurname("Doe");
        person4.setAge(30);
        person4.setEyeColour("brown");

        litebridge.save(person1, person2, person3, person4);

        // Delete DTO directly
        assertNotNull(litebridge.select(Person.class).where(PersonMapping.name).eq("Bob").oneOrNull());
        litebridge.delete(person2);
        assertNull(litebridge.select(Person.class).where(PersonMapping.name).eq("Bob").oneOrNull());

        // Delete DTO via query
        assertNotNull(litebridge.select(Person.class).where(PersonMapping.name).eq("Alice").oneOrNull());
        litebridge.delete(Person.class, p -> p.where(PersonMapping.name).eq("Alice").and(PersonMapping.age).eq(20));
        assertNull(litebridge.select(Person.class).where(PersonMapping.name).eq("Alice").oneOrNull());

        // Delete all Person records
        assertEquals(2, litebridge.select(Person.class).list().size());
        litebridge.delete(Person.class);
        assertEquals(0, litebridge.select(Person.class).list().size());
    }

    @TestTemplate
    @DisplayName("Update DTOs, no transactions (autocommit)")
    void update(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Test mappings specify uppercase schema/table/column names, so skip Postgres
        assumeTrue(!dbEnv.getName().equals("PostgreSQL"));

        // Given
        tableMapper.registerPersonAndAccountDtoTableMappings(litebridge, true);

        final Person person1 = new Person();
        person1.setName("Alice");
        person1.setSurname("Smith");
        person1.setAge(20);
        person1.setEyeColour("blue");

        final Account account = new Account();
        account.setName("Account 1");
        account.setBalance(BigInteger.valueOf(1000));
        account.setOwner(person1);
        person1.setAccounts(List.of(account));

        final Person person2 = new Person();
        person2.setName("Bob");
        person2.setSurname("Jones");
        person2.setAge(22);
        person2.setEyeColour("brown");

        litebridge.save(person1, person2);
        assumeTrue(litebridge.select(Person.class).list().size() == 2);
        assumeTrue(litebridge.select(Person.class).where(PersonMapping.name).eq("Jane").oneOrNull() == null);

        // Update the person's first name directly
        person1.setName("Jane");
        litebridge.update(person1);
        assertNotNull(litebridge.select(Person.class).where("name").eq("Jane").oneOrNull());

        // Update multiple records for the Person DTO via a query
        litebridge.update(Person.class, p -> p
                .set(PersonMapping.name).to("John")
                .set(PersonMapping.surname).to("Doe")
                .set(PersonMapping.age).to(18)
                .where(PersonMapping.age).gt(18));

        assertTrue(litebridge.select(Person.class).stream().allMatch(p -> p.getName().equals("John") && p.getSurname().equals("Doe") && p.getAge() == 18));

        // Adjust the age of all persons
        litebridge.update(Person.class, p ->
                p.set(PersonMapping.age).increment()
                        .where(PersonMapping.surname).eq("Doe"));

        assertTrue(litebridge.select(Person.class).stream().allMatch(p -> p.getAge() == 19));

        // Update a specific record using the SQL API
        litebridge.update(tableMapper.qualifyName("PERSON"), p ->
                p.set(PersonMapping.eyeColour).to("unknown")
                        .where(PersonMapping.eyeColour).eq("blue"));

        assertEquals(1, litebridge.select(Person.class).stream().filter(p -> p.getEyeColour().equals("unknown")).count());
    }
}