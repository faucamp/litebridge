package org.litebridge.orm.nativesql;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
