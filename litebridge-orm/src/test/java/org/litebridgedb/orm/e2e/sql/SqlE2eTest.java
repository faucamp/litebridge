package org.litebridgedb.orm.e2e.sql;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import org.litebridgedb.db.spi.Row;
import org.litebridgedb.orm.e2e.AbstractE2eTest;
import org.litebridgedb.orm.e2e.basic.dto.Person;
import org.litebridgedb.orm.e2e.setup.DbEnvDtoTableMapper;
import org.litebridgedb.orm.expression.Fn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.litebridgedb.orm.expression.Fn.c;

class SqlE2eTest extends AbstractE2eTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqlE2eTest.class);

    @TestTemplate
    @DisplayName("Select all records")
    void selectAll(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Given
        final String personTableName = tableMapper.qualifyName("PERSON");
        insertTestPersonRecords(personTableName);

        // When
        LOGGER.info("Selecting all records");
        final List<Row> result =
                litebridge.select().from(personTableName)
                        .orderBy(tableMapper.transformColumnName("PERSON_ID")).asc()
                        .list();

        // Then
        assertEquals(2, result.size());
        final Row row1 = result.getFirst();
        assertEquals(5, row1.columnStream().count());
        assertNumberEquals(1, row1.column(tableMapper.transformColumnName("PERSON_ID")).orElseThrow().value());
        assertEquals("Alice", row1.column(tableMapper.transformColumnName("FIRST_NAME")).orElseThrow().value());
        assertEquals("Smith", row1.column(tableMapper.transformColumnName("SURNAME")).orElseThrow().value());
        assertNumberEquals(20, row1.column(tableMapper.transformColumnName("AGE")).orElseThrow().value());
        assertEquals("brown", row1.column(tableMapper.transformColumnName("EYE_COLOUR")).orElseThrow().value());
        final Row row2 = result.get(1);
        assertEquals(5, row2.columnStream().count());
        assertNumberEquals(2, row2.column(tableMapper.transformColumnName("PERSON_ID")).orElseThrow().value());
        assertEquals("Bob", row2.column(tableMapper.transformColumnName("FIRST_NAME")).orElseThrow().value());
        assertEquals("Johnson", row2.column(tableMapper.transformColumnName("SURNAME")).orElseThrow().value());
        assertNull(row2.column(tableMapper.transformColumnName("EYE_COLOUR")).orElseThrow().value());
        assertNumberEquals(30, row2.column(tableMapper.transformColumnName("AGE")).orElseThrow().value());
    }

    @TestTemplate
    @DisplayName("Select specific expressions and filter records using a query")
    void selectQuery(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Given
        final String personTableName = tableMapper.qualifyName("PERSON");
        insertTestPersonRecords(personTableName);

        // When
        LOGGER.info("Selecting specific expressions and filtering records using a query");
        final List<Row> result =
                litebridge.select(tableMapper.transformColumnName("FIRST_NAME"),
                                tableMapper.transformColumnName("SURNAME"),
                                tableMapper.transformColumnName("AGE")).from(personTableName)
                        .where(tableMapper.transformColumnName("AGE")).gt(18)
                        .and(tableMapper.transformColumnName("AGE")).lt(25)
                        .list();

        // Then
        assertEquals(1, result.size());
        assertEquals(3, result.getFirst().columnStream().count());
        assertEquals("Alice", result.getFirst().column(tableMapper.transformColumnName("FIRST_NAME")).orElseThrow().value());
        assertEquals("Smith", result.getFirst().column(tableMapper.transformColumnName("SURNAME")).orElseThrow().value());
        assertNumberEquals(20, result.getFirst().column(tableMapper.transformColumnName("AGE")).orElseThrow().value());
    }

    @TestTemplate
    @DisplayName("Select records using SQL and map results to Person objects")
    void selectMapToDto(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Given
        final String personTableName = tableMapper.qualifyName("PERSON");
        insertTestPersonRecords(personTableName);
        tableMapper.registerPersonDtoTableMapping(litebridge);

        // When
        LOGGER.info("Selecting specific expressions and filtering records using a query");
        final List<Person> result =
                litebridge.select(tableMapper.transformColumnName("FIRST_NAME"),
                                tableMapper.transformColumnName("SURNAME"),
                                tableMapper.transformColumnName("AGE")).from(personTableName)
                        .where(tableMapper.transformColumnName("AGE")).gt(18)
                        .and(tableMapper.transformColumnName("AGE")).lt(25)
                        .orderBy(tableMapper.transformColumnName("PERSON_ID")).asc()
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
        insertTestPersonRecords(personTableName);
        insertTestAccountRecords(accountTableName);

        // When
        LOGGER.info("Selecting with a JOIN USING clause");
        final List<Row> result =
                litebridge.select(
                                c(personTableName, tableMapper.transformColumnName("FIRST_NAME")),
                                c(personTableName, tableMapper.transformColumnName("SURNAME")),
                                c(personTableName, tableMapper.transformColumnName("AGE")),
                                c(accountTableName, tableMapper.transformColumnName("ACCOUNT_ID")),
                                c(accountTableName, tableMapper.transformColumnName("ACCOUNT_NAME")))
                        .from(personTableName)
                        .join(accountTableName).using(tableMapper.transformColumnName("PERSON_ID"))
                        .list();

        // Then
        assertEquals(2, result.size());
        final Row row1 = result.getFirst();
        assertEquals(5, row1.columnStream().count());
        assertEquals("Alice", row1.column(tableMapper.transformColumnName("FIRST_NAME")).orElseThrow().value());
        assertEquals("Smith", row1.column(tableMapper.transformColumnName("SURNAME")).orElseThrow().value());
        assertNumberEquals(20, row1.column(tableMapper.transformColumnName("AGE")).orElseThrow().value());
        assertNumberEquals(1, row1.column(tableMapper.transformColumnName("ACCOUNT_ID")).orElseThrow().value());
        assertEquals("Alice's Account", row1.column(tableMapper.transformColumnName("ACCOUNT_NAME")).orElseThrow().value());
        final Row row2 = result.get(1);
        assertEquals(5, row2.columnStream().count());
        assertEquals("Bob", row2.column(tableMapper.transformColumnName("FIRST_NAME")).orElseThrow().value());
        assertEquals("Johnson", row2.column(tableMapper.transformColumnName("SURNAME")).orElseThrow().value());
        assertNumberEquals(30, row2.column(tableMapper.transformColumnName("AGE")).orElseThrow().value());
        assertNumberEquals(2, row2.column(tableMapper.transformColumnName("ACCOUNT_ID")).orElseThrow().value());
        assertEquals("Bob's Account", row2.column(tableMapper.transformColumnName("ACCOUNT_NAME")).orElseThrow().value());
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
        insertTestPersonRecords(personTableName);

        // When
        final List<Row> result =
                litebridge.select(Fn.c(tableMapper.transformColumnName("AGE")), Fn.count())
                        .from(personTableName)
                        .groupBy(tableMapper.transformColumnName("AGE"))
                        .orderBy(tableMapper.transformColumnName("AGE")).asc()
                        .list();

        // Then
        assertEquals(2, result.size());
        assertEquals(2, result.getFirst().columns().size());
        final Row row1 = result.getFirst();
        assertEquals(20, ((Number) row1.column(tableMapper.transformColumnName("AGE")).orElseThrow().value()).intValue());
        assertEquals(1, ((Number) row1.column(tableMapper.transformColumnName("COUNT(*)")).orElseThrow().value()).intValue());
        final Row row2 = result.get(1);
        assertEquals(30, ((Number) row2.column(tableMapper.transformColumnName("AGE")).orElseThrow().value()).intValue());
        assertEquals(1, ((Number) row2.column(tableMapper.transformColumnName("COUNT(*)")).orElseThrow().value()).intValue());
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
                .where(Fn.c("PERSON_ID")).in(1L, 2L)
                .list();

        assertEquals(2, results.size());

        final List<Row> results2 = litebridge.select()
                .from(personTableName)
                .where(Fn.c("PERSON_ID")).notIn(1L, 2L)
                .list();

        assertTrue(results2.isEmpty());

        // Using single value
        final List<Row> results3 = litebridge.select()
                .from(personTableName)
                .where("PERSON_ID").in(1L)
                .list();

        assertEquals(1, results3.size());

        final List<Row> results4 = litebridge.select()
                .from(personTableName)
                .where("PERSON_ID").notIn(1L)
                .list();

        assertEquals(1, results4.size());

        // Using a list
        final List<Long> ids = List.of(1L, 2L);
        final List<Row> results5 = litebridge.select()
                .from(personTableName)
                .where("PERSON_ID").in(ids)
                .list();

        assertEquals(2, results5.size());

        final List<Row> results6 = litebridge.select()
                .from(personTableName)
                .where("PERSON_ID").notIn(ids)
                .list();

        assertTrue(results6.isEmpty());

        // Using a subselect
        final List<Row> results7 = litebridge.select()
                .from(personTableName)
                .where("PERSON_ID").in(sub ->
                        sub.select("PERSON_ID")
                                .from(personTableName)
                                .where("FIRST_NAME").eq("Bob"))
                .list();

        assertEquals(1, results7.size());

        final List<Row> results8 = litebridge.select()
                .from(personTableName)
                .where("PERSON_ID").notIn(sub ->
                        sub.select("PERSON_ID")
                                .from(personTableName)
                                .where("FIRST_NAME").eq("Alice"))
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
                .where(Fn.c("SURNAME")).like("%ohnso%")
                .one();

        assertTrue(results.isPresent());
    }

    private void insertTestPersonRecords(final String personTableName) throws SQLException {
        try (final Connection connection = dbEnv.getDataSource().getConnection()) {
            try (final PreparedStatement preparedStatement = connection.prepareStatement(sql("INSERT INTO " + personTableName + " (PERSON_ID, FIRST_NAME, SURNAME, AGE, EYE_COLOUR) VALUES (?, ?, ?, ?, ?)"))) {
                insertPerson(1L, "Alice", "Smith", 20, "brown", preparedStatement);
                insertPerson(2L, "Bob", "Johnson", 30, null, preparedStatement);
            }
        }
    }

    private void insertTestAccountRecords(final String accountTableName) throws SQLException {
        try (final Connection connection = dbEnv.getDataSource().getConnection()) {
            try (final PreparedStatement preparedStatement = connection.prepareStatement(sql("INSERT INTO " + accountTableName + " (ACCOUNT_ID, ACCOUNT_NAME, BALANCE, PERSON_ID) VALUES (?, ?, ?, ?)"))) {
                insertAccount(1L, "Alice's Account", 1000L, 1L, preparedStatement);
                insertAccount(2L, "Bob's Account", 2000L, 2L, preparedStatement);
            }
        }
    }

    private String sql(final String sql) {
        return dbEnv.getName().equals("SQLite") ? sql.replace("LB.", "") : sql;
    }

    private void assertNumberEquals(final long expected, final Object actual) {
        if (actual instanceof Number number) {
            assertEquals(expected, number.longValue());
        } else {
            assertEquals(BigDecimal.valueOf(expected), actual);
        }
    }

    private void insertPerson(final Long personId, final String firstName, final String surname, final int age, final String eyeColour, final PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setLong(1, personId);
        preparedStatement.setString(2, firstName);
        preparedStatement.setString(3, surname);
        preparedStatement.setInt(4, age);
        preparedStatement.setString(5, eyeColour);
        preparedStatement.execute();
    }

    private void insertAccount(final Long accountId, final String accountName, final Long balance, final Long personId, final PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setLong(1, accountId);
        preparedStatement.setString(2, accountName);
        preparedStatement.setLong(3, balance);
        preparedStatement.setLong(4, personId);
        preparedStatement.execute();
    }
}