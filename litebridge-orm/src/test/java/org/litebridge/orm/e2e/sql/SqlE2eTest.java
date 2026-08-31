package org.litebridge.orm.e2e.sql;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.update.UpdateResult;
import org.litebridge.orm.e2e.AbstractE2eTest;
import org.litebridge.orm.e2e.basic.dto.Person;
import org.litebridge.orm.e2e.setup.DbEnvDtoTableMapper;
import org.litebridge.orm.expression.Fn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.litebridge.orm.expression.Fn.c;

class SqlE2eTest extends AbstractE2eTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqlE2eTest.class);

    @TestTemplate
    @DisplayName("Select all records")
    void selectAll(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Given
        final String personTableName = tableMapper.qualifyName("PERSON");
        final String personId = tableMapper.transformColumnName("PERSON_ID");
        final String firstName = tableMapper.transformColumnName("FIRST_NAME");
        final String surname = tableMapper.transformColumnName("SURNAME");
        final String age = tableMapper.transformColumnName("AGE");
        final String eyeColour = tableMapper.transformColumnName("EYE_COLOUR");
        insertTestPersonRecords(personTableName);

        // When
        LOGGER.info("Selecting all records");
        final List<Row> result =
                litebridge.select().from(personTableName)
                        .orderBy(personId).asc()
                        .list();

        // Then
        assertEquals(2, result.size());
        final Row row1 = result.getFirst();
        assertEquals(5, row1.columnStream().count());
        assertNumberEquals(1, row1.column(personId).orElseThrow().value());
        assertEquals("Alice", row1.column(firstName).orElseThrow().value());
        assertEquals("Smith", row1.column(surname).orElseThrow().value());
        assertNumberEquals(20, row1.column(age).orElseThrow().value());
        assertEquals("brown", row1.column(eyeColour).orElseThrow().value());
        final Row row2 = result.get(1);
        assertEquals(5, row2.columnStream().count());
        assertNumberEquals(2, row2.column(personId).orElseThrow().value());
        assertEquals("Bob", row2.column(firstName).orElseThrow().value());
        assertEquals("Johnson", row2.column(surname).orElseThrow().value());
        assertNull(row2.column(eyeColour).orElseThrow().value());
        assertNumberEquals(30, row2.column(age).orElseThrow().value());
    }

    @TestTemplate
    @DisplayName("Select with limit/offset")
    void select_limitOffset(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Given
        final String personTableName = tableMapper.qualifyName("PERSON");
        litebridge.insert(personTableName, i -> i
                .into("PERSON_ID", "FIRST_NAME", "SURNAME", "AGE", "EYE_COLOUR")
                .values(1L, "Alice", "Smith", 20, "brown")
                .values(2L, "Bob", "Johnson", 30, null)
                .values(3L, "Charlie", "Brown", 20, "blue")
        );

        // No offset/limit
        final List<Row> result1 =
                litebridge.select().from(personTableName)
                        .orderBy(tableMapper.transformColumnName("PERSON_ID")).asc()
                        .list();
        assertEquals(3, result1.size());

        // Offset only
        final List<Row> result2 =
                litebridge.select().from(personTableName)
                        .orderBy(tableMapper.transformColumnName("PERSON_ID")).asc()
                        .offset(1)
                        .list();
        assertEquals(2, result2.size());
        assertNumberEquals(2L, result2.getFirst().column("PERSON_ID").orElseThrow().value());
        assertNumberEquals(3L, result2.getLast().column("PERSON_ID").orElseThrow().value());

        // Limit only
        final List<Row> result3 =
                litebridge.select().from(personTableName)
                        .orderBy(tableMapper.transformColumnName("PERSON_ID")).asc()
                        .limit(1)
                        .list();
        assertEquals(1, result3.size());
        assertNumberEquals(1L, result3.getFirst().column("PERSON_ID").orElseThrow().value());

        // Limit and offset
        final List<Row> result4 =
                litebridge.select().from(personTableName)
                        .orderBy(tableMapper.transformColumnName("PERSON_ID")).asc()
                        .limit(1).offset(1)
                        .list();
        assertEquals(1, result4.size());
        assertNumberEquals(2L, result4.getFirst().column("PERSON_ID").orElseThrow().value());
    }

    @TestTemplate
    @DisplayName("Select specific expressions and filter records using a query")
    void selectQuery(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Given
        final String personTableName = tableMapper.qualifyName("PERSON");
        final String firstName = tableMapper.transformColumnName("FIRST_NAME");
        final String surname = tableMapper.transformColumnName("SURNAME");
        final String age = tableMapper.transformColumnName("AGE");
        insertTestPersonRecords(personTableName);

        // When
        LOGGER.info("Selecting specific expressions and filtering records using a query");
        final List<Row> result =
                litebridge.select(firstName, surname, age)
                        .from(personTableName)
                        .where(tableMapper.transformColumnName("AGE")).gt(18)
                        .and(tableMapper.transformColumnName("AGE")).lt(25)
                        .list();

        // Then
        assertEquals(1, result.size());
        assertEquals(3, result.getFirst().columnStream().count());
        assertEquals("Alice", result.getFirst().column(firstName).orElseThrow().value());
        assertEquals("Smith", result.getFirst().column(surname).orElseThrow().value());
        assertNumberEquals(20, result.getFirst().column(age).orElseThrow().value());
    }

    @TestTemplate
    @DisplayName("Select records using SQL and map results to Person objects")
    void selectMapToDto(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Given
        final String personTableName = tableMapper.qualifyName("PERSON");
        final String personId = tableMapper.transformColumnName("PERSON_ID");
        final String firstName = tableMapper.transformColumnName("FIRST_NAME");
        final String surname = tableMapper.transformColumnName("SURNAME");
        final String age = tableMapper.transformColumnName("AGE");
        insertTestPersonRecords(personTableName);
        tableMapper.registerPersonDtoTableMapping(litebridge);

        // When
        LOGGER.info("Selecting specific expressions and filtering records using a query");
        final List<Person> result =
                litebridge.select(firstName, surname, age)
                        .from(personTableName)
                        .where(age).gt(18)
                        .and(age).lt(25)
                        .orderBy(personId).asc()
                        .stream()
                        .map(row -> litebridge.toDto(row, Person.class))
                        .toList();

        // Then
        assertEquals(1, result.size());
        final Person person = result.getFirst();
        assertEquals("Alice", person.getName());
        assertEquals("Smith", person.getSurname());
        assertEquals(20L, person.getAge());
        assertNull(person.getEyeColour());
    }

    @TestTemplate
    @DisplayName("Select with a JOIN USING clause")
    void selectJoinUsing(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Given
        final String personTableName = tableMapper.qualifyName("PERSON");
        final String accountTableName = tableMapper.qualifyName("ACCOUNT");
        final String firstName = tableMapper.transformColumnName("FIRST_NAME");
        final String surname = tableMapper.transformColumnName("SURNAME");
        final String age = tableMapper.transformColumnName("AGE");
        final String accountId = tableMapper.transformColumnName("ACCOUNT_ID");
        final String accountName = tableMapper.transformColumnName("ACCOUNT_NAME");
        final String personId = tableMapper.transformColumnName("PERSON_ID");
        insertTestPersonRecords(personTableName);
        insertTestAccountRecords(accountTableName);

        // When
        LOGGER.info("Selecting with a JOIN USING clause");
        final List<Row> result =
                litebridge.select(
                                c(personTableName, firstName),
                                c(personTableName, surname),
                                c(personTableName, age),
                                c(accountTableName, accountId),
                                c(accountTableName, accountName))
                        .from(personTableName)
                        .join(accountTableName).using(personId)
                        .list();

        // Then
        assertEquals(2, result.size());
        final Row row1 = result.getFirst();
        assertEquals(5, row1.columnStream().count());
        assertEquals("Alice", row1.column(firstName).orElseThrow().value());
        assertEquals("Smith", row1.column(surname).orElseThrow().value());
        assertNumberEquals(20, row1.column(age).orElseThrow().value());
        assertNumberEquals(1, row1.column(accountId).orElseThrow().value());
        assertEquals("Alice's Account", row1.column(accountName).orElseThrow().value());
        final Row row2 = result.get(1);
        assertEquals(5, row2.columnStream().count());
        assertEquals("Bob", row2.column(firstName).orElseThrow().value());
        assertEquals("Johnson", row2.column(surname).orElseThrow().value());
        assertNumberEquals(30, row2.column(age).orElseThrow().value());
        assertNumberEquals(2, row2.column(accountId).orElseThrow().value());
        assertEquals("Bob's Account", row2.column(accountName).orElseThrow().value());
    }

    @TestTemplate
    @DisplayName("Select COUNT()")
    void selectCount(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Given
        final String personTableName = tableMapper.qualifyName("PERSON");
        insertTestPersonRecords(personTableName);

        // When
        LOGGER.info("Selecting specific expressions and filtering records using a query");
        final Row result = litebridge.select(Fn.count()).from(personTableName)
                .where(tableMapper.transformColumnName("AGE")).gt(18)
                .and(tableMapper.transformColumnName("AGE")).lt(25)
                .oneOrThrow();

        // Then
        assertEquals(1, result.size());
        assertInstanceOf(Number.class, result.column(0).value());
        assertEquals(1L, ((Number) result.column(0).value()).longValue());
    }

    @TestTemplate
    @DisplayName("Delete records")
    void delete(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Given
        final String personTableName = tableMapper.qualifyName("PERSON");
        insertTestPersonRecords(personTableName);
        assertEquals(2, litebridge.select().from(personTableName).list().size());

        // When
        litebridge.delete(personTableName, p -> p.where(tableMapper.transformColumnName("AGE")).gt(20));

        // Then
        assertEquals(1, litebridge.select().from(personTableName).list().size());
    }

    @TestTemplate
    @DisplayName("Update records")
    void update(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Given
        final String personTableName = tableMapper.qualifyName("PERSON");
        insertTestPersonRecords(personTableName);
        assumeTrue(litebridge.select().from(personTableName).where(tableMapper.transformColumnName("AGE")).lt(50).list().size() == 2);

        // When
        litebridge.update(personTableName, p -> p.set(tableMapper.transformColumnName("AGE")).to(50)
                .where(tableMapper.transformColumnName("FIRST_NAME")).eq("Bob"));

        // Then
        assertEquals(1, litebridge.select().from(personTableName).where(tableMapper.transformColumnName("AGE")).lt(50).list().size());
    }

    @TestTemplate
    @DisplayName("Select using GROUP BY")
    void selectGroupBy(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Given
        final String personTableName = tableMapper.qualifyName("PERSON");
        litebridge.insert(personTableName, i -> i
                .into("PERSON_ID", "FIRST_NAME", "SURNAME", "AGE", "EYE_COLOUR")
                .values(1L, "Alice", "Smith", 20, "brown")
                .values(2L, "Bob", "Johnson", 30, null)
                .values(3L, "Charlie", "Brown", 20, "blue")
        );

        // Select with group by
        final List<Row> result =
                litebridge.select(Fn.c(tableMapper.transformColumnName("AGE")), Fn.count())
                        .from(personTableName)
                        .groupBy(tableMapper.transformColumnName("AGE"))
                        .orderBy(tableMapper.transformColumnName("AGE")).asc()
                        .list();

        assertEquals(2, result.size());
        assertEquals(2, result.getFirst().columns().size());
        final Row row1 = result.getFirst();
        assertEquals(20, ((Number) row1.column(tableMapper.transformColumnName("AGE")).orElseThrow().value()).intValue());
        assertEquals(2, ((Number) row1.column(tableMapper.transformColumnName("COUNT(*)")).orElseThrow().value()).intValue());
        final Row row2 = result.get(1);
        assertEquals(30, ((Number) row2.column(tableMapper.transformColumnName("AGE")).orElseThrow().value()).intValue());
        assertEquals(1, ((Number) row2.column(tableMapper.transformColumnName("COUNT(*)")).orElseThrow().value()).intValue());

        // Select with group by and having
        final List<Row> result2 =
                litebridge.select(Fn.c(tableMapper.transformColumnName("AGE")), Fn.count())
                        .from(personTableName)
                        .groupBy(tableMapper.transformColumnName("AGE"))
                        .having(Fn.count()).gt(1)
                        .orderBy(tableMapper.transformColumnName("AGE")).asc()
                        .list();

        assertEquals(1, result2.size());
        assertEquals(2, result2.getFirst().columns().size());
        final Row row = result2.getFirst();
        assertEquals(20, ((Number) row.column(tableMapper.transformColumnName("AGE")).orElseThrow().value()).intValue());
        assertEquals(2, ((Number) row.column(tableMapper.transformColumnName("COUNT(*)")).orElseThrow().value()).intValue());
    }

    @TestTemplate
    @DisplayName("Select IN and NOT IN")
    void select_in(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Setup data
        final String personTableName = tableMapper.qualifyName("PERSON");
        insertTestPersonRecords(personTableName);

        // Using variable paratemeters/array
        final List<Row> results = litebridge.select()
                .from(personTableName)
                .where(Fn.c(tableMapper.transformColumnName("PERSON_ID"))).in(1L, 2L)
                .list();

        assertEquals(2, results.size());

        final List<Row> results2 = litebridge.select()
                .from(personTableName)
                .where(Fn.c(tableMapper.transformColumnName("PERSON_ID"))).notIn(1L, 2L)
                .list();

        assertTrue(results2.isEmpty());

        // Using single value
        final List<Row> results3 = litebridge.select()
                .from(personTableName)
                .where(tableMapper.transformColumnName("PERSON_ID")).in(1L)
                .list();

        assertEquals(1, results3.size());

        final List<Row> results4 = litebridge.select()
                .from(personTableName)
                .where(tableMapper.transformColumnName("PERSON_ID")).notIn(1L)
                .list();

        assertEquals(1, results4.size());

        // Using a list
        final List<Long> ids = List.of(1L, 2L);
        final List<Row> results5 = litebridge.select()
                .from(personTableName)
                .where(tableMapper.transformColumnName("PERSON_ID")).in(ids)
                .list();

        assertEquals(2, results5.size());

        final List<Row> results6 = litebridge.select()
                .from(personTableName)
                .where(tableMapper.transformColumnName("PERSON_ID")).notIn(ids)
                .list();

        assertTrue(results6.isEmpty());

        // Using a subselect
        final List<Row> results7 = litebridge.select()
                .from(personTableName)
                .where(tableMapper.transformColumnName("PERSON_ID")).in(sub ->
                        sub.select(tableMapper.transformColumnName("PERSON_ID"))
                                .from(personTableName)
                                .where(tableMapper.transformColumnName("FIRST_NAME")).eq("Bob"))
                .list();

        assertEquals(1, results7.size());

        final List<Row> results8 = litebridge.select()
                .from(personTableName)
                .where(tableMapper.transformColumnName("PERSON_ID")).notIn(sub ->
                        sub.select(tableMapper.transformColumnName(tableMapper.transformColumnName("PERSON_ID")))
                                .from(personTableName)
                                .where(tableMapper.transformColumnName("FIRST_NAME")).eq("Alice"))
                .list();

        assertEquals(1, results8.size());
    }

    @TestTemplate
    @DisplayName("Select LIKE")
    void select_like(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Setup data
        final String personTableName = tableMapper.qualifyName("PERSON");
        insertTestPersonRecords(personTableName);

        // Using variable paratemeters/array
        final Optional<Row> results = litebridge.select()
                .from(personTableName)
                .where(Fn.c(tableMapper.transformColumnName("SURNAME"))).like("%ohnso%")
                .one();

        assertTrue(results.isPresent());
    }

    @TestTemplate
    @DisplayName("Select LIKE")
    void nativeSql(final DbEnvDtoTableMapper tableMapper) throws Exception {
        final String tableName = tableMapper.qualifyName("PERSON");
        final String personIdColumn = tableMapper.transformColumnName("PERSON_ID");
        final String firstNameColumn = tableMapper.transformColumnName("FIRST_NAME");
        final String surnameColumn = tableMapper.transformColumnName("SURNAME");

        // Insert data using native SQL
        final UpdateResult updateResult = litebridge.nativeSql().execute(
                "INSERT INTO %s (%s, %s, %s) VALUES (?, ?, ?)".formatted(tableName, personIdColumn, firstNameColumn, surnameColumn),
                123L, "Name1", "Surname1");

        assertEquals(1, updateResult.rowsAffected());

        // Query using a native SQL query, positional bind parameters
        final List<Row> rows = litebridge.nativeSql().query(
                "SELECT * FROM %s WHERE %s LIKE ?".formatted(tableName, firstNameColumn),
                "%me1");

        assertEquals(1, rows.size());

        // Query using a native SQL query, named bind parameters
        final List<Row> rows2 = litebridge.nativeSql().query(
                "SELECT * FROM %s WHERE %s LIKE :firstName AND %s = :surname AND %s <> :firstName".formatted(tableName, firstNameColumn, surnameColumn, surnameColumn),
                Map.of("firstName", "%me1",
                        "surname", "Surname1"));

        assertEquals(1, rows2.size());

        // Query without bind parameters
        final List<Row> rows3 = litebridge.nativeSql().query("SELECT COUNT(*) FROM %s".formatted(tableName));
        assertEquals(1, rows3.size());

        // Native query and map result back to a DTO
        tableMapper.registerPersonDtoTableMapping(litebridge);

        final Person person = litebridge.nativeSql().query(
                        "SELECT * FROM %s WHERE %s = ?".formatted(tableName, personIdColumn),
                        123L)
                .stream()
                .map(row -> litebridge.toDto(row, Person.class))
                .findFirst().orElseThrow();

        assertEquals("Name1", person.getName());
    }

    @TestTemplate
    @DisplayName("Insert specific columns")
    void insert(final DbEnvDtoTableMapper tableMapper) throws Exception {
        litebridge.insert(tableMapper.qualifyName("PERSON"), i -> i
                .into("PERSON_ID", "FIRST_NAME", "SURNAME")
                .values(1, "Alice", "Smith")
                .values(2, "Bob", "Johnson")
                .values(3, "Charlie", "Brown"));
    }

    private void insertTestPersonRecords(final String personTableName) throws SQLException {
        litebridge.insert(personTableName, i -> i
                .into("PERSON_ID", "FIRST_NAME", "SURNAME", "AGE", "EYE_COLOUR")
                .values(1L, "Alice", "Smith", 20, "brown")
                .values(2L, "Bob", "Johnson", 30, null)
        );
    }

    private void insertTestAccountRecords(final String accountTableName) throws SQLException {
        litebridge.insert(accountTableName, i -> i
                .into("ACCOUNT_ID", "ACCOUNT_NAME", "BALANCE", "PERSON_ID")
                .values(1L, "Alice's Account", 1000L, 1L)
                .values(2L, "Bob's Account", 2000L, 2L));
    }

    private void assertNumberEquals(final long expected, final Object actual) {
        if (actual instanceof Number number) {
            assertEquals(expected, number.longValue());
        } else {
            assertEquals(BigDecimal.valueOf(expected), actual);
        }
    }
}