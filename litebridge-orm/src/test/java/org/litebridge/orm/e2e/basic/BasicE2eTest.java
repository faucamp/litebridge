package org.litebridge.orm.e2e.basic;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import org.litebridge.db.spi.Row;
import org.litebridge.orm.LitebridgeInspector;
import org.litebridge.orm.config.RelatedDtoStrategy;
import org.litebridge.orm.e2e.AbstractE2eTest;
import org.litebridge.orm.e2e.basic.dto.Account;
import org.litebridge.orm.e2e.basic.dto.Address;
import org.litebridge.orm.e2e.basic.dto.Person;
import org.litebridge.orm.e2e.basic.dto.PersonAccount;
import org.litebridge.orm.e2e.basic.meta.AccountMeta;
import org.litebridge.orm.e2e.basic.meta.PersonMeta;
import org.litebridge.orm.e2e.setup.DbEnvDtoTableMapper;
import org.litebridge.orm.engine.QueryPlanCache;
import org.litebridge.orm.expression.Fn;
import org.litebridge.orm.persistence.DtoEntityMapping;
import org.litebridge.orm.persistence.EntityDtoMapper;
import org.litebridge.orm.tx.Transaction;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.litebridge.orm.api.spec.FieldMapping.f;

public class BasicE2eTest extends AbstractE2eTest {

    @TestTemplate
    @DisplayName("Select DTO and join fetch related DTOs")
    void select_fetchRelatedDtos(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Register DTO-table mappings
        tableMapper.registerPersonAndAccountDtoTableMappings(litebridge);

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
        assertEquals(account, result);
        assertEquals(person, result.getOwner());

        // Retrieve the person record using a type-safe metamodel
        final Account result2 = litebridge.select(Account.class)
                .join(Person.class).on(AccountMeta.owner)
                .where(AccountMeta.id).eq(person.getId())
                .oneOrThrow();

        assertEquals(account, result2);
        assertEquals(person, result2.getOwner());

        // Reverse the join
        final Person resultPerson = litebridge.select(Person.class)
                .join(Account.class).on(PersonMeta.accounts)
                .where(PersonMeta.name).eq("Alice")
                .oneOrThrow();

        assertEquals(person.getName(), resultPerson.getName());
        assertNotNull(resultPerson.getAccounts());
        assertEquals(1, resultPerson.getAccounts().size());
        assertEquals(account, resultPerson.getAccounts().getFirst());
    }

    @TestTemplate
    @DisplayName("Select DTO without related DTOs")
    void select_doNotFetchRelatedDtos(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Register DTO-table mappings
        tableMapper.registerPersonAndAccountDtoTableMappings(litebridge);

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
        assertEquals(account.getId(), result.getId());
        assertNull(result.getOwner());

        // Since the data was loaded from the database, saving it again should do nothing
        litebridge.save(result);

        // Execute the same query, but this time create a partially-constructed related DTO
        final Account result2 = litebridge.select(Account.class, RelatedDtoStrategy.PARTIAL_OBJECT_IF_NO_JOIN)
                .where("id").eq(person.getId())
                .oneOrThrow();

        // Then
        assertNotNull(result2.getOwner());
        assertNotEquals(person, result2.getOwner());
        assertEquals(person.getId(), result2.getOwner().getId());
        assertNull(result2.getOwner().getName());
        assertNull(result2.getOwner().getSurname());
        assertNotNull(result2.getOwner().getAccounts());
        assertEquals(1, result2.getOwner().getAccounts().size());
        assertEquals(result2, result2.getOwner().getAccounts().getFirst());
        assertNull(result2.getOwner().getEyeColour());
        assertEquals(0, result2.getOwner().getAge());
    }

    @TestTemplate
    @DisplayName("Select DTO with nested conditional where clause")
    void select_nestedLogicalConditions(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Register DTO-table mappings
        tableMapper.registerPersonAndAccountDtoTableMappings(litebridge);

        final Person person = new Person();
        person.setName("Alice");
        person.setSurname("Smith");
        person.setAge(20);
        person.setEyeColour("blue");

        final Person person2 = new Person();
        person2.setName("Alice");
        person2.setSurname("Jones");
        person2.setAge(35);
        person2.setEyeColour("green");

        final Person person3 = new Person();
        person3.setName("Alice");
        person3.setSurname("West");
        person3.setAge(29);
        person3.setEyeColour("green");

        litebridge.saveAll(person, person2, person3);

        // Query using field names
        final Person result = litebridge.select(Person.class)
                .where("name").eq("Alice")
                .and(q -> q
                        .where("surname").eq("Jones")
                        .or("age").eq(21)
                        .or(q2 -> q2
                                .where("eyeColour").eq("green")
                                .and("age").gt(35)))
                .oneOrThrow();

        assertEquals(person2, result);

        // Similar query using metamodels
        final Person result2 = litebridge.select(Person.class)
                .where(PersonMeta.name).eq("Alice")
                .and(q -> q
                        .where(PersonMeta.surname).eq("Jones")
                        .or(PersonMeta.age).eq(21)
                        .or(q2 -> q2
                                .where(PersonMeta.eyeColour).eq("green")
                                .and(PersonMeta.age).gt(35)))
                .and(PersonMeta.id).isNotNull()
                .oneOrThrow();

        assertEquals(person2, result2);

        // Nested query on initial where clause
        final List<Person> result3 = litebridge.select(Person.class)
                .where(q -> q
                        .where(PersonMeta.id).eq(3)
                        .or(PersonMeta.eyeColour).neq("green"))
                .list();

        assertEquals(2, result3.size());
    }

    @TestTemplate
    @DisplayName("Select, join multiple tables")
    void nestedDtos_multiJoin(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Register DTO-table mappings
        tableMapper.registerPersonAndAccountDtoTableMappings(litebridge);
        registerAddressTableMapping(tableMapper);

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

        final Address address = new Address();
        address.setId(123L);
        address.setPerson(person);
        address.setAddress("123 Main St");
        person.setAddresses(List.of(address));

        // Cascade save everything
        litebridge.save(person);

        // Retrieve the person record and associated address and account
        final Person result = litebridge.select(Person.class)
                .join(Account.class).on("accounts")
                .join(Address.class).on("addresses")
                .where("id").eq(person.getId())
                .oneOrThrow();

        // Then
        assertEquals(person, result);
        assertNotNull(result.getAccounts());
        assertEquals(1, result.getAccounts().size());
        assertEquals(account, result.getAccounts().getFirst());
        assertNotNull(result.getAddresses());
        assertEquals(1, result.getAddresses().size());
        assertEquals(address, result.getAddresses().getFirst());

        // Retrieve the person record and associated address and account, with conditions on the join table
        final Person result2 = litebridge.select(Person.class)
                .join(Account.class).on("accounts")
                .join(Address.class).on("addresses")
                .where(Fn.f(Person.class, "id")).eq(person.getId())
                .and(Fn.f(Address.class, "id")).eq(address.getId())
                .oneOrThrow();

        // Then
        assertEquals(person, result2);
        assertNotNull(result2.getAccounts());
        assertEquals(1, result2.getAccounts().size());
        assertEquals(account, result2.getAccounts().getFirst());
        assertNotNull(result2.getAddresses());
        assertEquals(1, result2.getAddresses().size());
        assertEquals(address, result2.getAddresses().getFirst());
    }

    @TestTemplate
    @DisplayName("Nested DTOs mapped to separate tables, cascading save, no transactions (autocommit)")
    void nestedDtos_oneTablePerDto_cascadeSave_autoCommit(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Register DTO-table mappings
        tableMapper.registerPersonAndAccountDtoTableMappings(litebridge);

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

    @TestTemplate
    @DisplayName("Nested DTOs mapped to separate tables, cascading save in transaction")
    void nestedDtos_oneTablePerDto_cascadeSave_transaction(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Register DTO-table mappings
        tableMapper.registerPersonAndAccountDtoTableMappings(litebridge);

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
                .join(Account.class).on("accounts")
                .where("id").eq(person.getId())
                .oneOrThrow();
        assertEquals(person.getId(), fetchedPerson.getId());
        assertEquals(person.getName(), fetchedPerson.getName());
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

    /**
     * Disabled - known regression; not a focus right now.
     */
    @TestTemplate
    @DisplayName("Single DTO mapped to multiple tables")
    void singleDto_multipleTables(final DbEnvDtoTableMapper tableMapper) throws Exception {
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
        tableMapper.registerPersonAndAccountDtoTableMappings(litebridge);

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
        litebridge.saveAll(entityDtoMapper.entities(personAccount));

        // Load the indidual entities and reconstruct the composite DTO
        final Person person = litebridge.select(Person.class).where("id").eq(personAccount.getId()).oneOrThrow();
        final Account account = litebridge.select(Account.class).where("id").eq(personAccount.getAccountId()).oneOrThrow();
        final PersonAccount result = entityDtoMapper.dto(person, account);

        // Then
        assertEquals(personAccount, result);
    }

    @TestTemplate
    @DisplayName("Select DTO by ID")
    void selectDtoById(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Register DTO-table mappings
        tableMapper.registerPersonAndAccountDtoTableMappings(litebridge);

        // Setup DTOs
        final Person person = new Person();
        person.setId(123L);
        person.setName("Alice");
        person.setSurname("Smith");
        person.setAge(20);
        person.setEyeColour("blue");

        litebridge.save(person);

        // When/Then
        final Optional<Person> result1 = litebridge.select(Person.class).withId(123L);
        assertTrue(result1.isPresent());
        assertEquals(person, result1.get());

        final Optional<Person> result2 = litebridge.select(Person.class).withId(234L);
        assertTrue(result2.isEmpty());

        final Person result3 = litebridge.select(Person.class).withIdOrNull(123L);
        assertEquals(person, result3);

        final Person result4 = litebridge.select(Person.class).withIdOrNull(234L);
        assertNull(result4);

        final Person result5 = litebridge.select(Person.class).withIdOrThrow(123L);
        assertEquals(person, result3);

        assertThrows(NoSuchElementException.class, () -> litebridge.select(Person.class).withIdOrThrow(234L));

        final Person result6 = litebridge.select(Person.class).withIdOrThrow(123L, TestException::new);
        assertEquals(person, result3);

        assertThrows(TestException.class, () -> litebridge.select(Person.class).withIdOrThrow(234L, TestException::new));
    }

    @TestTemplate
    @DisplayName("Delete DTOs, no transactions (autocommit)")
    void delete_autoCommit(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Register DTO-table mappings
        tableMapper.registerPersonAndAccountDtoTableMappings(litebridge);

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

        final Person person5 = new Person();
        person5.setName("Henry");
        person5.setSurname("Jones");
        person5.setAge(45);
        person5.setEyeColour("brown");

        litebridge.saveAll(person1, person2, person3, person4, person5);
        assumeTrue(() -> litebridge.select(Person.class).list().size() == 5);

        // Delete DTO directly
        assertTrue(litebridge.select(Person.class).where("name").eq("Bob").one().isPresent());
        litebridge.delete(person2);
        assertFalse(litebridge.select(Person.class).where("name").eq("Bob").one().isPresent());

        // Delete DTO via query
        assertTrue(litebridge.select(Person.class).where("name").eq("Alice").one().isPresent());
        litebridge.delete(Person.class, p -> p
                .where("name").eq("Alice")
                .and("age").eq(20)
                .and(q -> q
                        .where("surname").eq("Jones")
                        .or("surname").eq("Smith"))
                .and(q -> q
                        .where("surname").neq("Doe")
                        .or("age").lte(0)));
        assertFalse(litebridge.select(Person.class).where("name").eq("Alice").one().isPresent());

        // Delete via metamodel
        assertTrue(litebridge.select(Person.class).where("name").eq("Henry").one().isPresent());
        litebridge.delete(Person.class, p -> p
                .where(PersonMeta.name).eq("Henry")
                .and(PersonMeta.age).eq(45));
        assertFalse(litebridge.select(Person.class).where("name").eq("Henry").one().isPresent());

        // Delete all Person records
        assertEquals(2, litebridge.select(Person.class).list().size());
        litebridge.delete(Person.class);
        assertEquals(0, litebridge.select(Person.class).list().size());
    }

    @TestTemplate
    @DisplayName("Update DTOs, no transactions (autocommit)")
    void update(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Given
        tableMapper.registerPersonAndAccountDtoTableMappings(litebridge);

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

        litebridge.saveAll(person1, person2);
        assumeTrue(litebridge.select(Person.class).list().size() == 2);
        assertNull(litebridge.select(Person.class).where("name").eq("Jane").oneOrNull());

        // Update the person's first name directly
        person1.setName("Jane");
        litebridge.update(person1);
        assertNotNull(litebridge.select(Person.class).where("name").eq("Jane").oneOrNull());

        // Update a specific record using a metamodel
        litebridge.update(Person.class, p ->
                p.set(PersonMeta.eyeColour).to("green")
                        .where(PersonMeta.name).eq("Jane"));

        assertEquals(1, litebridge.select(Person.class).stream().filter(p -> p.getEyeColour().equals("green")).count());

        // Update multiple records for the Person DTO via a query
        litebridge.update(Person.class, p -> p
                .set("name").to("John")
                .set("surname").to("Doe")
                .set("age").to(18)
                .where("age").gt(18)
                .or(q -> q
                        .where("age").lte(0)
                        .or("age").gte(99)));

        assertTrue(litebridge.select(Person.class).stream().allMatch(p -> p.getName().equals("John") && p.getSurname().equals("Doe") && p.getAge() == 18));

        // Adjust the age of all persons
        litebridge.update(Person.class, p ->
                p.set("age").increment()
                        .where("surname").eq("Doe"));

        assertTrue(litebridge.select(Person.class).stream().allMatch(p -> p.getAge() == 19));

        // Update a specific record using the SQL API
        litebridge.update(tableMapper.qualifyName("PERSON"), p ->
                p.set(tableMapper.transformColumnName("EYE_COLOUR")).to("unknown")
                        .where(tableMapper.transformColumnName("EYE_COLOUR")).eq("green"));

        assertEquals(1, litebridge.select(Person.class).stream().filter(p -> p.getEyeColour().equals("unknown")).count());
    }

    @TestTemplate
    @DisplayName("Select specific fields, names only")
    void select_specificFields_strings(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Register DTO-table mappings
        tableMapper.registerPersonAndAccountDtoTableMappings(litebridge);

        // Setup data
        final Person[] persons = new Person[3];
        for (int i = 0; i < 3; i++) {
            persons[i] = new Person();
            persons[i].setName("Name" + i);
            persons[i].setSurname("Surname" + i);
            persons[i].setAge(20 + i);
        }

        litebridge.saveAll(persons);

        // Read and populate specific fields only
        final List<Person> result = litebridge.select("id", "surname").from(Person.class).orderBy("id").asc().list();

        assertEquals(3, result.size());
        for (int i = 0; i < 3; i++) {
            final Person p = result.get(i);
            assertNotNull(p.getId());
            assertEquals(persons[i].getSurname(), p.getSurname());
            assertNull(p.getName());
            assertEquals(0, p.getAge());
        }
    }

    @TestTemplate
    @DisplayName("Select specific fields using expressions")
    void select_specificFields_expressions(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Register DTO-table mappings
        tableMapper.registerPersonAndAccountDtoTableMappings(litebridge);

        // Setup data
        final Person[] persons = new Person[3];
        for (int i = 0; i < 3; i++) {
            persons[i] = new Person();
            persons[i].setName("Name" + i);
            persons[i].setSurname("Surname" + i);
            persons[i].setAge(20 + i);
        }

        final Account account = new Account();
        account.setId(123L);
        account.setName("Test account");
        account.setBalance(BigInteger.TEN);
        persons[0].setAccounts(List.of(account));

        litebridge.saveAll(persons);

        // Select and populate specific fields only
        final List<Person> result = litebridge.select(Fn.f("id"), Fn.f("surname"))
                .from(Person.class)
                .orderBy(Fn.f("id")).asc()
                .list();

        assertEquals(3, result.size());
        for (int i = 0; i < 3; i++) {
            final Person p = result.get(i);
            assertNotNull(p.getId());
            assertEquals(persons[i].getSurname(), p.getSurname());
            assertNull(p.getName());
            assertEquals(0, p.getAge());
        }

        // Select and populate specific fields only, with a related DTO strategy

    }

    @TestTemplate
    @DisplayName("Select with subselect")
    void select_subselect(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Register DTO-table mappings
        tableMapper.registerPersonAndAccountDtoTableMappings(litebridge);

        // Setup data
        final Person[] persons = new Person[3];
        for (int i = 0; i < 3; i++) {
            persons[i] = new Person();
            persons[i].setId(1L + i);
            persons[i].setName("Name" + i);
            persons[i].setSurname("Surname" + i);
            persons[i].setAge(20 + i);
        }

        litebridge.saveAll(persons);

        // Select DTO via subselect
        final Person result = litebridge.select().from(Person.class)
                .where("id").eq(sub ->
                        sub.select("id").from(Person.class)
                                .where("name").eq("Name1"))
                .oneOrThrow();

        assertEquals(persons[1], result);
    }

    @TestTemplate
    @DisplayName("Select grouping by")
    void select_groupBy(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Register DTO-table mappings
        tableMapper.registerPersonAndAccountDtoTableMappings(litebridge);

        // Setup data
        final Person[] persons = new Person[3];
        for (int i = 0; i < 3; i++) {
            persons[i] = new Person();
            persons[i].setId(1L + i);
            persons[i].setName("Name" + i);
            persons[i].setSurname("Surname" + i);
        }

        persons[0].setAge(20);
        persons[1].setAge(25);
        persons[2].setAge(25);

        litebridge.saveAll(persons);

        final List<Row> results = litebridge.select(Fn.row(Fn.convert(Fn.f("age"), Integer.class), Fn.convert(Fn.count(), Long.class)))
                .from(Person.class)
                .groupBy("age")
                .orderBy("age").asc()
                .list();

        assertEquals(2, results.size());
        assertEquals(20, results.get(0).column(tableMapper.transformColumnName("AGE")).orElseThrow().value());
        assertEquals(1L, results.get(0).column(tableMapper.transformColumnName("COUNT(*)")).orElseThrow().value());
        assertEquals(25, results.get(1).column(tableMapper.transformColumnName("AGE")).orElseThrow().value());
        assertEquals(2L, results.get(1).column(tableMapper.transformColumnName("COUNT(*)")).orElseThrow().value());

        // Use a query expression in the groupBy() clause
        final List<Row> results2 = litebridge.select(Fn.row(Fn.convert(Fn.f("age"), Integer.class), Fn.convert(Fn.count(), Long.class)))
                .from(Person.class)
                .groupBy(Fn.f("age"))
                .orderBy(Fn.f("age")).asc()
                .list();

        assertEquals(2, results2.size());
        assertEquals(20, results2.get(0).column(tableMapper.transformColumnName("AGE")).orElseThrow().value());
        assertEquals(1L, results2.get(0).column(tableMapper.transformColumnName("COUNT(*)")).orElseThrow().value());
        assertEquals(25, results2.get(1).column(tableMapper.transformColumnName("AGE")).orElseThrow().value());
        assertEquals(2L, results2.get(1).column(tableMapper.transformColumnName("COUNT(*)")).orElseThrow().value());
    }

    @TestTemplate
    @DisplayName("Select IN and NOT IN")
    void select_in(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Register DTO-table mappings
        tableMapper.registerPersonAndAccountDtoTableMappings(litebridge);

        // Setup data
        final Person[] persons = new Person[3];
        for (int i = 0; i < 3; i++) {
            persons[i] = new Person();
            persons[i].setId(1L + i);
            persons[i].setName("Name" + i);
            persons[i].setSurname("Surname" + i);
        }

        litebridge.saveAll(persons);

        // Using variable paratemeters/array
        final List<Person> results = litebridge.select()
                .from(Person.class)
                .where("id").in(1L, 2L)
                .list();

        assertEquals(2, results.size());

        final List<Person> results2 = litebridge.select()
                .from(Person.class)
                .where("id").notIn(1L, 2L)
                .list();

        assertEquals(1, results2.size());

        // Using single value
        final List<Person> results3 = litebridge.select()
                .from(Person.class)
                .where("id").in(1L)
                .list();

        assertEquals(1, results3.size());

        final List<Person> results4 = litebridge.select()
                .from(Person.class)
                .where("id").notIn(1L)
                .list();

        assertEquals(2, results4.size());

        // Using a list
        final List<Long> ids = List.of(1L, 2L);
        final List<Person> results5 = litebridge.select()
                .from(Person.class)
                .where("id").in(ids)
                .list();

        assertEquals(2, results5.size());

        final List<Person> results6 = litebridge.select()
                .from(Person.class)
                .where("id").notIn(ids)
                .list();

        assertEquals(1, results6.size());

        // Using a subselect
        final List<Person> results7 = litebridge.select()
                .from(Person.class)
                .where("id").in(sub ->
                        sub.select("id")
                                .from(Person.class)
                                .where("name").eq("Name1"))
                .list();

        assertEquals(1, results7.size());

        final List<Person> results8 = litebridge.select()
                .from(Person.class)
                .where("id").notIn(sub ->
                        sub.select("id")
                                .from(Person.class)
                                .where("name").eq("Name1"))
                .list();

        assertEquals(2, results8.size());
    }

    @TestTemplate
    @DisplayName("Verify QueryPlanCache hits for DTO operations")
    void cacheHits_dtoOperations(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Register DTO-table mappings
        tableMapper.registerPersonAndAccountDtoTableMappings(litebridge);

        final QueryPlanCache cache = LitebridgeInspector.getQueryPlanCache(litebridge);
        cache.clear();

        // 1. Insert
        final Person person1 = new Person();
        person1.setName("Alice");
        person1.setSurname("Smith");
        litebridge.insert(person1);
        final int sizeAfterInsert = cache.size();
        assertTrue(sizeAfterInsert > 0, "Cache should not be empty after insert");

        final Person person2 = new Person();
        person2.setName("Bob");
        person2.setSurname("Doe");
        litebridge.insert(person2);
        assertEquals(sizeAfterInsert, cache.size(), "Cache size should not increase for second similar insert");

        // 2. Update
        person1.setName("Alice Updated");
        litebridge.update(person1);
        final int sizeAfterUpdate = cache.size();
        assertTrue(sizeAfterUpdate > sizeAfterInsert, "Cache size should increase after first update");

        person2.setName("Bob Updated");
        litebridge.update(person2);
        assertEquals(sizeAfterUpdate, cache.size(), "Cache size should not increase for second similar update");

        // 3. Delete
        litebridge.delete(person1);
        final int sizeAfterDelete = cache.size();
        assertTrue(sizeAfterDelete > sizeAfterUpdate, "Cache size should increase after first delete");

        litebridge.delete(person2);
        assertEquals(sizeAfterDelete, cache.size(), "Cache size should not increase for second similar delete");
    }

    @TestTemplate
    @DisplayName("Inserting specific fields via metamodel")
    void insert(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Register DTO-table mappings
        tableMapper.registerPersonAndAccountDtoTableMappings(litebridge);

        final Person person = new Person();
        person.setName("Test");
        person.setSurname("User");
        litebridge.insert(person);

        // Insert multiple rows of  specific fields using DTO and metamodel
        litebridge.insert(Person.class, i -> i
                .into(PersonMeta.id, PersonMeta.name, PersonMeta.surname, PersonMeta.age)
                .values(76, "Alice", "Smith", 25)
                .values(101, "Bob", "Jones", 30));

        // Insert multiple rows of specific fields using DTO and metamodel using sequence-generated columns
        // The Oracle database provider handles these differently from the previous insert
        litebridge.insert(Person.class, i -> i
                .into(PersonMeta.name, PersonMeta.surname, PersonMeta.age)
                .values("Alice", "Smith", 25)
                .values("Bob", "Jones", 30));

        // Insert specific fields using DTO and field names
        litebridge.insert(Person.class, i -> i
                .into("id", "name", "surname", "age")
                .values(500, "Robert", "Frost", 28)
                .values(501, "James", "Wilson", 34));
    }

    @TestTemplate
    @DisplayName("Verify QueryPlanCache hits for cascade operations")
    void cacheHits_cascadeOperations(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Register DTO-table mappings
        tableMapper.registerPersonAndAccountDtoTableMappings(litebridge);

        final QueryPlanCache cache = LitebridgeInspector.getQueryPlanCache(litebridge);
        cache.clear();

        // Create Person with Account (Cascade Save)
        final Person person = new Person();
        person.setName("Alice");
        person.setSurname("Smith");
        final Account account = new Account();
        account.setName("Account 1");
        account.setBalance(BigInteger.valueOf(1000));
        account.setOwner(person);
        person.setAccounts(new ArrayList<>(List.of(account)));

        litebridge.save(person);
        final int sizeAfterSave = cache.size();
        // Should contain entries for Person (Insert) and Account (Insert)
        assertTrue(sizeAfterSave >= 2, "Cache should contain at least 2 entries after cascade save");

        final Person person2 = new Person();
        person2.setName("Bob");
        person2.setSurname("Doe");
        final Account account2 = new Account();
        account2.setName("Account 2");
        account2.setBalance(BigInteger.valueOf(2000));
        account2.setOwner(person2);
        person2.setAccounts(new ArrayList<>(List.of(account2)));

        litebridge.save(person2);
        assertEquals(sizeAfterSave, cache.size(), "Cache size should not increase for second similar cascade save");
    }

    @TestTemplate
    @DisplayName("Select LIKE")
    void select_like(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Register DTO-table mappings
        tableMapper.registerPersonAndAccountDtoTableMappings(litebridge);

        // Setup data
        final Person[] persons = new Person[3];
        for (int i = 0; i < 3; i++) {
            persons[i] = new Person();
            persons[i].setId(1L + i);
            persons[i].setName("Name" + i);
            persons[i].setSurname("Surname" + i);
        }

        litebridge.saveAll(persons);

        // Using variable paratemeters/array
        final Optional<Person> results = litebridge.select()
                .from(Person.class)
                .where(Fn.f("name")).like("%me1")
                .one();

        assertTrue(results.isPresent());
    }

    private void registerAddressTableMapping(final DbEnvDtoTableMapper tableMapper) {
        if (dbEnv.getName().equals("PostgreSQL")) {
            litebridge.register(Address.class, rc -> rc
                    .mapToTable("lb.address")
                    .with(spec -> spec.mapField("id").toColumn("address_id"))
                    .with(spec -> spec.mapField("person").toColumn("person_id").joinUsing())
                    .with(spec -> spec.mapField("address").toColumn("address")));
        } else {
            litebridge.register(Address.class, rc -> rc
                    .mapToTable(tableMapper.qualifyName("ADDRESS"))
                    .with(spec -> spec.mapField("id").toColumn("ADDRESS_ID"))
                    .with(spec -> spec.mapField("person").toColumn("PERSON_ID").joinUsing())
                    .with(spec -> spec.mapField("address").toColumn("ADDRESS")));
        }
    }

    private static class TestException extends RuntimeException {
    }
}