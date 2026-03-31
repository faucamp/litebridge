package org.litebridge.orm.e2e.sql;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Row;
import org.litebridge.orm.e2e.AbstractE2eTest;
import org.litebridge.orm.e2e.basic.dto.Person;
import org.litebridge.orm.e2e.basic.mapping.DtoTableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.litebridge.db.spi.Column.c;
import static org.litebridge.orm.api.spec.TableSpec.t;

class SqlE2eTest extends AbstractE2eTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqlE2eTest.class);

    @Test
    @DisplayName("Select all records")
    void selectAll() throws Exception {
        // Given
        insertTestPersonRecords();

        // When
        LOGGER.info("Selecting all records");
        final List<Row> result =
                litebridge.select().from("LB", "PERSON")
                        .orderBy("PERSON_ID").asc()
                        .list();

        // Then
        assertEquals(2, result.size());
        final Row row1 = result.getFirst();
        assertEquals(5, row1.columnStream().count());
        assertEquals(1L, row1.column("PERSON_ID").orElseThrow().value());
        assertEquals("Alice", row1.column("FIRST_NAME").orElseThrow().value());
        assertEquals("Smith", row1.column("SURNAME").orElseThrow().value());
        assertEquals(20L, row1.column("AGE").orElseThrow().value());
        assertEquals("brown", row1.column("EYE_COLOUR").orElseThrow().value());
        final Row row2 = result.get(1);
        assertEquals(5, row2.columnStream().count());
        assertEquals(2L, row2.column("PERSON_ID").orElseThrow().value());
        assertEquals("Bob", row2.column("FIRST_NAME").orElseThrow().value());
        assertEquals("Johnson", row2.column("SURNAME").orElseThrow().value());
        assertNull(row2.column("EYE_COLOUR").orElseThrow().value());
        assertEquals(30L, row2.column("AGE").orElseThrow().value());
    }

    @Test
    @DisplayName("Select specific columns and filter records using a query")
    void selectQuery() throws Exception {
        // Given
        insertTestPersonRecords();

        // When
        LOGGER.info("Selecting specific columns and filtering records using a query");
        final List<Row> result =
                litebridge.select("FIRST_NAME", "SURNAME", "AGE").from("LB", "PERSON")
                        .where("AGE").gt(18)
                        .and("AGE").lt(25)
                        .list();

        // Then
        assertEquals(1, result.size());
        assertEquals(3, result.getFirst().columnStream().count());
        assertEquals("Alice", result.getFirst().column("FIRST_NAME").orElseThrow().value());
        assertEquals("Smith", result.getFirst().column("SURNAME").orElseThrow().value());
        assertEquals(20L, result.getFirst().column("AGE").orElseThrow().value());
    }

    @Test
    @DisplayName("Select records using SQL and map results to Person objects")
    void selectMapToDto() throws Exception {
        // Given
        insertTestPersonRecords();
        litebridge.register(Person.class, t("LB", "PERSON", DtoTableMap.Person));

        // When
        LOGGER.info("Selecting specific columns and filtering records using a query");
        final List<Person> result =
                litebridge.select("FIRST_NAME", "SURNAME", "AGE").from("LB", "PERSON")
                        .where("AGE").gt(18)
                        .and("AGE").lt(25)
                        .orderBy("PERSON_ID").asc()
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

    @Test
    @DisplayName("Select with a JOIN USING clause")
    void selectJoinUsing() throws Exception {
        // Given
        insertTestPersonRecords();
        insertTestAccountRecords();

        // When
        LOGGER.info("Selecting with a JOIN USING clause");
        final List<Row> result =
                litebridge.select(
                                c("PERSON", "FIRST_NAME"),
                                c("PERSON", "SURNAME"),
                                c("PERSON", "AGE"),
                                c("ACCOUNT", "ACCOUNT_ID"),
                                c("ACCOUNT", "ACCOUNT_NAME"))
                        .from("LB", "PERSON")
                        .join("LB", "ACCOUNT").using("PERSON_ID")
                        .list();

        // Then
        assertEquals(2, result.size());
        final Row row1 = result.getFirst();
        assertEquals(5, row1.columnStream().count());
        assertEquals("Alice", row1.column("FIRST_NAME").orElseThrow().value());
        assertEquals("Smith", row1.column("SURNAME").orElseThrow().value());
        assertEquals(20L, row1.column("AGE").orElseThrow().value());
        assertEquals(1L, row1.column("ACCOUNT_ID").orElseThrow().value());
        assertEquals("Alice's Account", row1.column("ACCOUNT_NAME").orElseThrow().value());
        final Row row2 = result.get(1);
        assertEquals(5, row2.columnStream().count());
        assertEquals("Bob", row2.column("FIRST_NAME").orElseThrow().value());
        assertEquals("Johnson", row2.column("SURNAME").orElseThrow().value());
        assertEquals(30L, row2.column("AGE").orElseThrow().value());
        assertEquals(2L, row2.column("ACCOUNT_ID").orElseThrow().value());
        assertEquals("Bob's Account", row2.column("ACCOUNT_NAME").orElseThrow().value());
    }

    @Test
    @DisplayName("Delete records")
    void delete() throws Exception {
        // Given
        insertTestPersonRecords();
        assertEquals(2, litebridge.select().from("LB", "PERSON").list().size());

        // When
        litebridge.delete()
                .from("LB.PERSON")
                .where("AGE").gt(20)
                .execute();

        // Then
        assertEquals(1, litebridge.select().from("LB", "PERSON").list().size());
    }

    private void insertTestPersonRecords() throws SQLException {
        final Connection connection = dataSource().getConnection();
        final PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO LB.PERSON (PERSON_ID, FIRST_NAME, SURNAME, AGE, EYE_COLOUR) VALUES (?, ?, ?, ?, ?)");
        insertPerson(1L, "Alice", "Smith", 20, "brown", preparedStatement);
        insertPerson(2L, "Bob", "Johnson", 30, null, preparedStatement);
        preparedStatement.close();
    }

    private void insertTestAccountRecords() throws SQLException {
        final Connection connection = dataSource().getConnection();
        final PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO LB.ACCOUNT (ACCOUNT_ID, ACCOUNT_NAME, BALANCE, PERSON_ID) VALUES (?, ?, ?, ?)");
        insertAccount(1L, "Alice's Account", 1000L, 1L, preparedStatement);
        insertAccount(2L, "Bob's Account", 2000L, 2L, preparedStatement);
        preparedStatement.close();
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