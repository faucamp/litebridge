package org.litebridgedb.orm.nativesql;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParsedSqlTest {

    @Test
    void testRecordMethods() {
        // Given
        final String sql = "SELECT * FROM TABLE WHERE COL = ?";
        final List<String> params = List.of("param1");
        final ParsedSql parsedSql = new ParsedSql(sql, params);

        // Then
        assertEquals(sql, parsedSql.sql());
        assertEquals(params, parsedSql.bindParameterNames());
        assertEquals(new ParsedSql(sql, params), parsedSql);
        assertEquals(new ParsedSql(sql, params).hashCode(), parsedSql.hashCode());
        assertEquals("ParsedSql[sql=" + sql + ", bindParameterNames=" + params + "]", parsedSql.toString());
    }
}
