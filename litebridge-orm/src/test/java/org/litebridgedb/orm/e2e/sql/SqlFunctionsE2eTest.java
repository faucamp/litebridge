package org.litebridgedb.orm.e2e.sql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import org.litebridgedb.db.spi.Row;
import org.litebridgedb.orm.e2e.AbstractE2eTest;
import org.litebridgedb.orm.e2e.setup.DbEnvDtoTableMapper;
import org.litebridgedb.orm.e2e.setup.DbEnvironment;
import org.litebridgedb.orm.expression.Fn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SqlFunctionsE2eTest extends AbstractE2eTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqlFunctionsE2eTest.class);
    private String personTableName;

    @Override
    @BeforeEach
    public void beforeEach(DbEnvironment env) throws Exception {
        super.beforeEach(env);

        // Setup data
        personTableName = env.getDtoTableMapper().qualifyName("PERSON");
        insertTestPersonRecords(personTableName);
    }

    @TestTemplate
    @DisplayName("COUNT()")
    void count(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Count the records
        final Row personCount = litebridge.select(Fn.count()).from(personTableName).oneOrThrow();
        assertEquals(1, personCount.size());
        assertEquals(1, personCount.columns().size());
        assertEquals(3, ((Number) personCount.column(0).value()).intValue());

        // Type override
        final Row personCountDouble = litebridge.select(Fn.convert(Fn.count(), Double.class)).from(personTableName).oneOrThrow();
        assertEquals(1, personCountDouble.size());
        assertEquals(3.0, personCountDouble.column(0).value());

        final Row personCountString = litebridge.select(Fn.convert(Fn.count(), String.class)).from(personTableName).oneOrThrow();
        assertEquals(1, personCountString.size());
        assertEquals("3", personCountString.column(0).value());
    }

    @TestTemplate
    @DisplayName("AVG()")
    void avg(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Column name only
        final Row averageAge = litebridge.select(Fn.avg(tableMapper.transformColumnName("AGE"))).from(personTableName).oneOrThrow();
        assertEquals(25, ((Number) averageAge.column(0).value()).intValue());

        // Nested column selector
        final Row averageAgeExpr = litebridge.select(Fn.avg(Fn.f(tableMapper.transformColumnName("AGE")))).from(personTableName).oneOrThrow();
        assertEquals(25, ((Number) averageAgeExpr.column(0).value()).intValue());

        // Type conversion: get the average age and convert the return type
        final Row averageAgeDouble = litebridge.select(Fn.convert(Fn.avg(tableMapper.transformColumnName("AGE")), Double.class)).from(personTableName).oneOrThrow();
        assertInstanceOf(Double.class, averageAgeDouble.column(0).value());
        assertEquals(25.0, averageAgeDouble.column(0).value());

        final Row averageAgeLong = litebridge.select(Fn.convert(Fn.avg(tableMapper.transformColumnName("AGE")), Long.class)).from(personTableName).oneOrThrow();
        assertInstanceOf(Long.class, averageAgeLong.column(0).value());
        assertEquals(25L, averageAgeLong.column(0).value());

        final Row averageAgeInteger = litebridge.select(Fn.convert(Fn.avg(tableMapper.transformColumnName("AGE")), Integer.class)).from(personTableName).oneOrThrow();
        assertInstanceOf(Integer.class, averageAgeInteger.column(0).value());
        assertEquals(25, averageAgeInteger.column(0).value());

        final Row averageAgeShort = litebridge.select(Fn.convert(Fn.avg(tableMapper.transformColumnName("AGE")), Short.class)).from(personTableName).oneOrThrow();
        assertInstanceOf(Short.class, averageAgeShort.column(0).value());
        assertEquals((short) 25, averageAgeShort.column(0).value());

        final Row averageAgeString = litebridge.select(Fn.convert(Fn.avg(tableMapper.transformColumnName("AGE")), String.class)).from(personTableName).oneOrThrow();
        assertInstanceOf(String.class, averageAgeString.column(0).value());

        if (dbEnv.getName().equals("SQLite")) {
            // SQLite returns a Double
            assertEquals("25.0", averageAgeString.column(0).value());
        } else {
            assertEquals("25", averageAgeString.column(0).value());
        }
    }

    @TestTemplate
    @DisplayName("MIN()")
    void min(final DbEnvDtoTableMapper tableMapper) throws Exception {
        final Row minAge = litebridge.select(Fn.min(tableMapper.transformColumnName("AGE"))).from(personTableName).oneOrThrow();
        assertEquals(1, minAge.size());
        assertEquals(20, ((Number) minAge.column(0).value()).intValue());

        final Row minAgeInt = litebridge.select(Fn.convert(Fn.min(tableMapper.transformColumnName("AGE")), Integer.class)).from(personTableName).oneOrThrow();
        assertEquals(20, ((Number) minAgeInt.column(0).value()).intValue());
    }

    @TestTemplate
    @DisplayName("MAX()")
    void max(final DbEnvDtoTableMapper tableMapper) throws Exception {
        final Row maxAge = litebridge.select(Fn.max(tableMapper.transformColumnName("AGE"))).from(personTableName).oneOrThrow();
        assertEquals(1, maxAge.size());
        assertEquals(30, ((Number) maxAge.column(0).value()).intValue());

        final Row maxAgeInt = litebridge.select(Fn.convert(Fn.max(tableMapper.transformColumnName("AGE")), Integer.class)).from(personTableName).oneOrThrow();
        assertEquals(30, maxAgeInt.column(0).value());
    }

    @TestTemplate
    @DisplayName("UPPER()")
    void upper(final DbEnvDtoTableMapper tableMapper) throws Exception {
        final List<Row> uppercaseNames = litebridge.select(Fn.upper(tableMapper.transformColumnName("FIRST_NAME"))).from(personTableName).list();
        assertLinesMatch(List.of("NAME0", "NAME1", "NAME2"), uppercaseNames.stream().flatMap(Row::columnStream)
                .map(rowColumn -> (String) rowColumn.value())
                .toList());

        // Get the uppercase names of the stored persons
        final List<Row> uppercaseNamesExpr = litebridge.select(Fn.upper(Fn.c(tableMapper.transformColumnName("FIRST_NAME")))).from(personTableName).list();
        assertLinesMatch(List.of("NAME0", "NAME1", "NAME2"), uppercaseNamesExpr.stream().flatMap(Row::columnStream)
                .map(rowColumn -> (String) rowColumn.value())
                .toList());
    }

    @TestTemplate
    @DisplayName("LOWER()")
    void lower(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Get the lowercase names of the stored persons
        final List<Row> lowercaseNames = litebridge.select(Fn.lower(tableMapper.transformColumnName("FIRST_NAME"))).from(personTableName).list();
        assertLinesMatch(List.of("name0", "name1", "name2"), lowercaseNames.stream().flatMap(Row::columnStream)
                .map(rowColumn -> (String) rowColumn.value())
                .toList());

        final List<Row> lowercaseNamesExpr = litebridge.select(Fn.lower(Fn.f(tableMapper.transformColumnName("FIRST_NAME")))).from(personTableName).list();
        assertLinesMatch(List.of("name0", "name1", "name2"), lowercaseNamesExpr.stream().flatMap(Row::columnStream)
                .map(rowColumn -> (String) rowColumn.value())
                .toList());
    }

    @TestTemplate
    @DisplayName("SUBSTRING()")
    void substring(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Get substrings of the surnames
        final List<Row> surnameSubstrings = litebridge.select(Fn.substring(tableMapper.transformColumnName("SURNAME"), 2, 5)).from(personTableName).list();
        assertLinesMatch(List.of("urnam", "urnam", "urnam"), surnameSubstrings.stream().flatMap(Row::columnStream)
                .map(rowColumn -> (String) rowColumn.value())
                .toList());

        // Nested SQL functions
        final List<Row> uppercaseSubstrings = litebridge.select(Fn.upper(Fn.substring(tableMapper.transformColumnName("SURNAME"), 4))).from(personTableName).list();
        assertLinesMatch(List.of("NAME0", "NAME1", "NAME2"), uppercaseSubstrings.stream().flatMap(Row::columnStream)
                .map(rowColumn -> (String) rowColumn.value())
                .toList());
    }

    @TestTemplate
    @DisplayName("ABS()")
    void abs(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Get the lowercase names of the stored persons
        final Row absAge = litebridge.select(Fn.abs(tableMapper.transformColumnName("AGE"))).from(personTableName).firstOrThrow();
        assertEquals(20, ((Number) absAge.column(0).value()).intValue());

        final Row absAgeExpr = litebridge.select(Fn.abs(Fn.f(tableMapper.transformColumnName("AGE")))).from(personTableName).firstOrNull();
        assertEquals(20, ((Number) absAgeExpr.column(0).value()).intValue());
    }

    @TestTemplate
    @DisplayName("Nested functions")
    void nestedFunctions(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Nested SQL functions
        final List<Row> uppercaseSubstrings = litebridge.select(Fn.upper(Fn.substring(tableMapper.transformColumnName("SURNAME"), 4))).from(personTableName).list();
        assertLinesMatch(List.of("NAME0", "NAME1", "NAME2"), uppercaseSubstrings.stream().flatMap(Row::columnStream)
                .map(rowColumn -> (String) rowColumn.value())
                .toList());
    }

    @TestTemplate
    @DisplayName("CURRENT_TIMESTAMP")
    void currentTimestamp(final DbEnvDtoTableMapper tableMapper) throws Exception {
        final Row sysdate = litebridge.select(Fn.currentTimestamp()).from(personTableName).firstOrThrow();
        assertEquals(1, sysdate.size());
        assertNotNull(sysdate.column(0).value());
    }

    private void insertTestPersonRecords(final String personTableName) throws SQLException {
        try (final Connection connection = dbEnv.getDataSource().getConnection()) {
            try (final PreparedStatement preparedStatement = connection.prepareStatement(sql("INSERT INTO " + personTableName + " (PERSON_ID, FIRST_NAME, SURNAME, AGE, EYE_COLOUR) VALUES (?, ?, ?, ?, ?)"))) {
                for (int i = 0; i < 3; i++) {
                    insertPerson((long) i, "Name" + i, "Surname" + i, 20 + (i * 5), null, preparedStatement);
                }
            }
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

    private String sql(final String sql) {
        return dbEnv.getName().equals("SQLite") ? sql.replace("LB.", "") : sql;
    }
}