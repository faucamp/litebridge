package org.litebridgedb.orm.e2e.basic;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import org.litebridgedb.db.spi.Row;
import org.litebridgedb.orm.config.RelatedDtoStrategy;
import org.litebridgedb.orm.e2e.AbstractE2eTest;
import org.litebridgedb.orm.e2e.basic.dto.Account;
import org.litebridgedb.orm.e2e.basic.dto.Address;
import org.litebridgedb.orm.e2e.basic.dto.Person;
import org.litebridgedb.orm.e2e.basic.dto.PersonAccount;
import org.litebridgedb.orm.e2e.basic.meta.PersonMeta;
import org.litebridgedb.orm.e2e.setup.DbEnvDtoTableMapper;
import org.litebridgedb.orm.expression.Fn;
import org.litebridgedb.orm.persistence.DtoEntityMapping;
import org.litebridgedb.orm.persistence.EntityDtoMapper;
import org.litebridgedb.orm.tx.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.litebridgedb.orm.api.spec.FieldMapping.f;

public class BasicE2eTest extends AbstractE2eTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicE2eTest.class);

    @TestTemplate
    @DisplayName("Select DTO and join fetch related DTOs")
    void nestedDtos_fetchRelatedDtos(final DbEnvDtoTableMapper tableMapper) throws Exception {
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
        assertEquals(person, result.getOwner());

        // Retrieve the person record using a type-safe metamodel
        final Person resultPerson = litebridge.select(Person.class)
                .where(PersonMeta.name).eq("Alice")
                .oneOrThrow();

    }

    @TestTemplate
    @DisplayName("Select DTO without related DTOs")
    void nestedDtos_donNotFetchRelatedDtos(final DbEnvDtoTableMapper tableMapper) throws Exception {
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
        assertNull(result2.getOwner().getAccounts());
        assertNull(result2.getOwner().getEyeColour());
        assertEquals(0, result2.getOwner().getAge());
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
        entityDtoMapper.entities(personAccount).forEach(litebridge::save);

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

        litebridge.save(person1, person2, person3, person4);

        // Delete DTO directly
        assertNotNull(litebridge.select(Person.class).where("name").eq("Bob").oneOrNull());
        litebridge.delete(person2);
        assertNull(litebridge.select(Person.class).where("name").eq("Bob").oneOrNull());

        // Delete DTO via query
        assertNotNull(litebridge.select(Person.class).where("name").eq("Alice").oneOrNull());
        litebridge.delete(Person.class, p -> p.where("name").eq("Alice").and("age").eq(20));
        assertNull(litebridge.select(Person.class).where("name").eq("Alice").oneOrNull());

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

        litebridge.save(person1, person2);
        assumeTrue(litebridge.select(Person.class).list().size() == 2);
        assumeTrue(litebridge.select(Person.class).where("name").eq("Jane").oneOrNull() == null);

        // Update the person's first name directly
        person1.setName("Jane");
        litebridge.update(person1);
        assertNotNull(litebridge.select(Person.class).where("name").eq("Jane").oneOrNull());

        // Update multiple records for the Person DTO via a query
        litebridge.update(Person.class, p -> p
                .set("name").to("John")
                .set("surname").to("Doe")
                .set("age").to(18)
                .where("age").gt(18));

        assertTrue(litebridge.select(Person.class).stream().allMatch(p -> p.getName().equals("John") && p.getSurname().equals("Doe") && p.getAge() == 18));

        // Adjust the age of all persons
        litebridge.update(Person.class, p ->
                p.set("age").increment()
                        .where("surname").eq("Doe"));

        assertTrue(litebridge.select(Person.class).stream().allMatch(p -> p.getAge() == 19));

        // Update a specific record using the SQL API
        litebridge.update(tableMapper.qualifyName("PERSON"), p ->
                p.set(tableMapper.transformColumnName("EYE_COLOUR")).to("unknown")
                        .where(tableMapper.transformColumnName("EYE_COLOUR")).eq("blue"));

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

        litebridge.save((Object[]) persons);

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

        litebridge.save((Object[]) persons);

        // Read and populate specific fields only
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

        litebridge.save((Object[]) persons);

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

        litebridge.save((Object[]) persons);

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

        litebridge.save((Object[]) persons);

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

        litebridge.save((Object[]) persons);

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