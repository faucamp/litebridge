package org.litebridgedb.orm.e2e.sql;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import org.litebridgedb.db.spi.Row;
import org.litebridgedb.orm.e2e.AbstractE2eTest;
import org.litebridgedb.orm.e2e.basic.dto.Person;
import org.litebridgedb.orm.e2e.setup.DbEnvDtoTableMapper;
import org.litebridgedb.orm.function.Functions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.litebridgedb.db.spi.Column.c;

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
        tableMapper.registerPersonDtoTableMapping(litebridge, false);

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

    //TODO: re-add
//    @TestTemplate
//    @DisplayName("Select with a JOIN USING clause")
//    void selectJoinUsing(final DbEnvDtoTableMapper tableMapper) throws Exception {
//        // Given
//        final String personTableName = tableMapper.qualifyName("PERSON");
//        final String accountTableName = tableMapper.qualifyName("ACCOUNT");
//        insertTestPersonRecords(personTableName);
//        insertTestAccountRecords(accountTableName);
//
//        // When
//        LOGGER.info("Selecting with a JOIN USING clause");
//        final List<Row> result =
//                litebridge.select(
//                                c(personTableName, tableMapper.transformColumnName("FIRST_NAME")),
//                                c(personTableName, tableMapper.transformColumnName("SURNAME")),
//                                c(personTableName, tableMapper.transformColumnName("AGE")),
//                                c(accountTableName, tableMapper.transformColumnName("ACCOUNT_ID")),
//                                c(accountTableName, tableMapper.transformColumnName("ACCOUNT_NAME")))
//                        .from(personTableName)
//                        .join(accountTableName).using(tableMapper.transformColumnName("PERSON_ID"))
//                        .list();
//
//        // Then
//        assertEquals(2, result.size());
//        final Row row1 = result.getFirst();
//        assertEquals(5, row1.columnStream().count());
//        assertEquals("Alice", row1.column(tableMapper.transformColumnName("FIRST_NAME")).orElseThrow().value());
//        assertEquals("Smith", row1.column(tableMapper.transformColumnName("SURNAME")).orElseThrow().value());
//        assertNumberEquals(20, row1.column(tableMapper.transformColumnName("AGE")).orElseThrow().value());
//        assertNumberEquals(1, row1.column(tableMapper.transformColumnName("ACCOUNT_ID")).orElseThrow().value());
//        assertEquals("Alice's Account", row1.column(tableMapper.transformColumnName("ACCOUNT_NAME")).orElseThrow().value());
//        final Row row2 = result.get(1);
//        assertEquals(5, row2.columnStream().count());
//        assertEquals("Bob", row2.column(tableMapper.transformColumnName("FIRST_NAME")).orElseThrow().value());
//        assertEquals("Johnson", row2.column(tableMapper.transformColumnName("SURNAME")).orElseThrow().value());
//        assertNumberEquals(30, row2.column(tableMapper.transformColumnName("AGE")).orElseThrow().value());
//        assertNumberEquals(2, row2.column(tableMapper.transformColumnName("ACCOUNT_ID")).orElseThrow().value());
//        assertEquals("Bob's Account", row2.column(tableMapper.transformColumnName("ACCOUNT_NAME")).orElseThrow().value());
//    }

//    @TestTemplate
//    @DisplayName("Select COUNT()")
//    void selectCount(final DbEnvDtoTableMapper tableMapper) throws Exception {
//        // Given
//        final String personTableName = tableMapper.qualifyName("PERSON");
//        insertTestPersonRecords(personTableName);
//
//        // When
//        LOGGER.info("Selecting specific expressions and filtering records using a query");
//        final List<Row> result =
//                litebridge.select(fn.count()).from(personTableName)
//                        .where(tableMapper.transformColumnName("AGE")).gt(18)
//                        .and(tableMapper.transformColumnName("AGE")).lt(25)
//                        .list();
//
//        // Then
//        assertEquals(1, result.size());
//        assertEquals(3, result.getFirst().columnStream().count());
//        assertEquals("Alice", result.getFirst().column(tableMapper.transformColumnName("FIRST_NAME")).orElseThrow().value());
//        assertEquals("Smith", result.getFirst().column(tableMapper.transformColumnName("SURNAME")).orElseThrow().value());
//        assertNumberEquals(20, result.getFirst().column(tableMapper.transformColumnName("AGE")).orElseThrow().value());
//    }

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

    private String tableName(final String tableName) {
        return dbEnv.getName().equals("SQLite") ? tableName.replace("LB.", "") : tableName;
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