package org.litebridgedb.orm.e2e.basic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import org.litebridgedb.orm.e2e.AbstractE2eTest;
import org.litebridgedb.orm.e2e.basic.dto.Person;
import org.litebridgedb.orm.e2e.setup.DbEnvDtoTableMapper;
import org.litebridgedb.orm.e2e.setup.DbEnvironment;
import org.litebridgedb.orm.expression.Fn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;

public class FunctionsE2eTest extends AbstractE2eTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(FunctionsE2eTest.class);

    @Override
    @BeforeEach
    public void beforeEach(DbEnvironment env) throws Exception {
        super.beforeEach(env);

        // Register DTO-table mappings
        env.getDtoTableMapper().registerPersonAndAccountDtoTableMappings(litebridge, false);

        // Setup data
        final Person[] persons = new Person[3];
        for (int i = 0; i < 3; i++) {
            persons[i] = new Person();
            persons[i].setName("Name" + i);
            persons[i].setSurname("Surname" + i);
            persons[i].setAge(20 + (i * 5));
        }

        litebridge.save((Object[]) persons);
    }

    @TestTemplate
    @DisplayName("COUNT()")
    void count(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Count the records
        final Long personCount = litebridge.select(Fn.count()).from(Person.class).oneOrThrow();
        assertEquals(3, personCount);
    }

    @TestTemplate
    @DisplayName("AVG()")
    void avg(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Column name only
        final Number averageAge = litebridge.select(Fn.avg("age")).from(Person.class).oneOrThrow();
        assertEquals(25, averageAge.intValue());

        // Nested column selector
        final Number averageAgeExpr = litebridge.select(Fn.avg(Fn.f("age"))).from(Person.class).oneOrThrow();
        assertEquals(25, averageAgeExpr.intValue());

        // Type conversion: get the average age and convert the return type
        final Double averageAgeDouble = litebridge.select(Fn.convert(Fn.avg("age"), Double.class)).from(Person.class).oneOrThrow();
        assertEquals(25.0, averageAgeDouble);

        final Long averageAgeLong = litebridge.select(Fn.convert(Fn.avg("age"), Long.class)).from(Person.class).oneOrThrow();
        assertEquals(25L, averageAgeLong);

        final Integer averageAgeInteger = litebridge.select(Fn.convert(Fn.avg("age"), Integer.class)).from(Person.class).oneOrThrow();
        assertEquals(25, averageAgeInteger);

        final Short averageAgeShort = litebridge.select(Fn.convert(Fn.avg("age"), Short.class)).from(Person.class).oneOrThrow();
        assertEquals((short) 25, averageAgeShort);

        final String averageAgeString = litebridge.select(Fn.convert(Fn.avg("age"), String.class)).from(Person.class).oneOrThrow();
        assertEquals("25", averageAgeString);
    }

    @TestTemplate
    @DisplayName("MIN()")
    void min(final DbEnvDtoTableMapper tableMapper) throws Exception {
        final Number minAge = litebridge.select(Fn.min("age")).from(Person.class).oneOrThrow();
        assertEquals(20, ((Number) minAge).intValue());

        final int minAgeInt = litebridge.select(Fn.convert(Fn.min("age"), Integer.class)).from(Person.class).oneOrThrow();
        assertEquals(20, minAgeInt);
    }

    @TestTemplate
    @DisplayName("MAX()")
    void max(final DbEnvDtoTableMapper tableMapper) throws Exception {
        final Number maxAge = litebridge.select(Fn.max("age")).from(Person.class).oneOrThrow();
        assertEquals(30, ((Number) maxAge).intValue());

        final int maxAgeInt = litebridge.select(Fn.convert(Fn.max("age"), Integer.class)).from(Person.class).oneOrThrow();
        assertEquals(30, maxAgeInt);
    }

    @TestTemplate
    @DisplayName("UPPER()")
    void upper(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Get the uppercase names of the stored persons
        final List<String> uppercaseNames = litebridge.select(Fn.upper("name")).from(Person.class).list();
        assertLinesMatch(List.of("NAME0", "NAME1", "NAME2"), uppercaseNames);

        // Get the uppercase names of the stored persons
        final List<String> uppercaseNamesExpr = litebridge.select(Fn.upper(Fn.f("name"))).from(Person.class).list();
        assertLinesMatch(List.of("NAME0", "NAME1", "NAME2"), uppercaseNamesExpr);
    }

    @TestTemplate
    @DisplayName("LOWER()")
    void lower(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Get the lowercase names of the stored persons
        final List<String> lowercaseNames = litebridge.select(Fn.lower("name")).from(Person.class).list();
        assertLinesMatch(List.of("name0", "name1", "name2"), lowercaseNames);

        final List<String> lowercaseNamesExpr = litebridge.select(Fn.lower(Fn.f("name"))).from(Person.class).list();
        assertLinesMatch(List.of("name0", "name1", "name2"), lowercaseNamesExpr);
    }

    @TestTemplate
    @DisplayName("SUBSTRING()")
    void substring(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Get substrings of the surnames
        final List<String> surnameSubstrings = litebridge.select(Fn.substring("surname", 2, 5)).from(Person.class).list();
        assertLinesMatch(List.of("urnam", "urnam", "urnam"), surnameSubstrings);

        // Nested SQL functions
        final List<String> uppercaseSubstrings = litebridge.select(Fn.upper(Fn.substring("surname", 4))).from(Person.class).list();
        assertLinesMatch(List.of("NAME0", "NAME1", "NAME2"), uppercaseSubstrings);
    }

    @TestTemplate
    @DisplayName("ABS()")
    void abs(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Get the lowercase names of the stored persons
        final Number absAge = litebridge.select(Fn.abs("age")).from(Person.class).firstOrThrow();
        assertEquals(20, absAge.intValue());

        final Number absAgeExpr = litebridge.select(Fn.abs(Fn.f("age"))).from(Person.class).firstOrNull();
        assertEquals(20, absAgeExpr.intValue());
    }

    @TestTemplate
    @DisplayName("Nested functions")
    void nestedFunctions(final DbEnvDtoTableMapper tableMapper) throws Exception {
        // Nested SQL functions
        final List<String> uppercaseSubstrings = litebridge.select(Fn.upper(Fn.substring("surname", 4))).from(Person.class).list();
        assertLinesMatch(List.of("NAME0", "NAME1", "NAME2"), uppercaseSubstrings);
    }
}