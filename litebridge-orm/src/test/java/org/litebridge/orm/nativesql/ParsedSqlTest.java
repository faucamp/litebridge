package org.litebridge.orm.nativesql;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.sql.PreparedSql;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParsedSqlTest {

    @Test
    void testRecordMethods() {
        // Given
        final String sql = "SELECT * FROM TABLE WHERE COL = ?";
        final List<String> params = List.of("param1");
        final ParsedSql parsedSql = new ParsedSql(sql, 1, params);

        // Then
        assertEquals(sql, parsedSql.sql());
        assertEquals(1, parsedSql.bindValueCount());
        assertEquals(params, parsedSql.bindValueNames());
    }

    @Test
    void testUsesNamedBindParameters() {
        // Given
        final ParsedSql withNamed = new ParsedSql("SELECT * FROM TABLE WHERE COL = ?", 1, List.of("param1"));
        final ParsedSql withoutNamed = new ParsedSql("SELECT * FROM TABLE WHERE COL = ?", 1, Collections.emptyList());

        // Then
        assertTrue(withNamed.usesNamedBindParameters());
        assertFalse(withoutNamed.usesNamedBindParameters());
    }

    @Test
    void testPrepareSql_withList() {
        // Given
        final ParsedSql parsedSql = new ParsedSql("SELECT * FROM TABLE WHERE COL = ?", 1, Collections.emptyList());

        // When
        final PreparedSql preparedSql = parsedSql.prepareSql(List.of("val1"));

        // Then
        assertNotNull(preparedSql);
        assertEquals("SELECT * FROM TABLE WHERE COL = ?", preparedSql.sql());
        assertEquals(1, preparedSql.bindValues().size());
        assertEquals("val1", preparedSql.bindValues().getFirst().value());
    }

    @Test
    void testPrepareSql_withArray() {
        // Given
        final ParsedSql parsedSql = new ParsedSql("SELECT * FROM TABLE WHERE COL = ?", 1, Collections.emptyList());

        // When
        final PreparedSql preparedSql = parsedSql.prepareSql(new Object[]{"val1"});

        // Then
        assertNotNull(preparedSql);
        assertEquals("SELECT * FROM TABLE WHERE COL = ?", preparedSql.sql());
        assertEquals(1, preparedSql.bindValues().size());
        assertEquals("val1", preparedSql.bindValues().getFirst().value());
    }

    @Test
    void testPrepareSql_withMap() {
        // Given
        final ParsedSql parsedSql = new ParsedSql("SELECT * FROM TABLE WHERE COL = ?", 1, List.of("param1"));

        // When
        final PreparedSql preparedSql = parsedSql.prepareSql(Map.of("param1", "val1"));

        // Then
        assertNotNull(preparedSql);
        assertEquals("SELECT * FROM TABLE WHERE COL = ?", preparedSql.sql());
        assertEquals(1, preparedSql.bindValues().size());
        assertEquals("val1", preparedSql.bindValues().getFirst().value());
    }

    @Test
    void testPrepareSql_withMap_emptyNamedParametersThrowsException() {
        // Given
        final ParsedSql parsedSql = new ParsedSql("SELECT * FROM TABLE WHERE COL = ?", 0, Collections.emptyList());

        // When / Then
        assertThrows(IllegalArgumentException.class, () -> parsedSql.prepareSql(Collections.emptyMap()));
    }

    @Test
    void testPrepareSql_validationFailureThrowsException() {
        // Given
        final ParsedSql parsedSql = new ParsedSql("SELECT * FROM TABLE WHERE COL = ?", 1, Collections.emptyList());

        // When / Then
        assertThrows(IllegalArgumentException.class, () -> parsedSql.prepareSql(List.of()));
        assertThrows(IllegalArgumentException.class, () -> parsedSql.prepareSql(new Object[0]));
        assertThrows(IllegalArgumentException.class, () -> parsedSql.prepareSql(Map.of()));
    }
}
