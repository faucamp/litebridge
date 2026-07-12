package org.litebridge.orm.nativesql;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SqlParserTest {

    @Test
    void testPrivateConstructor() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        // Given
        final Constructor<SqlParser> constructor = SqlParser.class.getDeclaredConstructor();
        assertTrue(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()));

        // When
        constructor.setAccessible(true);
        final SqlParser instance = constructor.newInstance();

        // Then
        assertNotNull(instance);
    }

    @Test
    void parseSql() {
        // Given
        final String sql = "SELECT * FROM LB.PERSON WHERE SURNAME = :name OR FIRST_NAME = :name OR AGE = :age";
        
        // When
        final ParsedSql result = SqlParser.parseSql(sql);

        // Then
        assertEquals("SELECT * FROM LB.PERSON WHERE SURNAME = ? OR FIRST_NAME = ? OR AGE = ?", result.sql());
        assertEquals(List.of("name", "name", "age"), result.bindParameterNames());
    }

    @Test
    void parseSql_quotes() {
        // Given
        final String sql = "SELECT * FROM LB.PERSON WHERE REMARK = 'Do not delete: active user' AND SURNAME = :name";

        // When
        final ParsedSql result = SqlParser.parseSql(sql);

        // Then
        assertEquals("SELECT * FROM LB.PERSON WHERE REMARK = 'Do not delete: active user' AND SURNAME = ?", result.sql());
        assertEquals(List.of("name"), result.bindParameterNames());
    }

    @Test
    void parseSql_castExpression() {
        // Given
        final String sql = "SELECT PERSON_ID::text FROM LB.PERSON WHERE SURNAME = :name";

        // When
        final ParsedSql result = SqlParser.parseSql(sql);

        // Then
        assertEquals("SELECT PERSON_ID::text FROM LB.PERSON WHERE SURNAME = ?", result.sql());
        assertEquals(List.of("name"), result.bindParameterNames());
    }

    @Test
    void parseSql_withTail() {
        // Given
        final String sql = "SELECT * FROM LB.PERSON WHERE SURNAME = :name AND 1=1";

        // When
        final ParsedSql result = SqlParser.parseSql(sql);

        // Then
        assertEquals("SELECT * FROM LB.PERSON WHERE SURNAME = ? AND 1=1", result.sql());
        assertEquals(List.of("name"), result.bindParameterNames());
    }
}