package org.litebridgedb.db.spi.sql;

import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PreparedSqlTest {

    @Test
    void preparedSql() {
        // Given
        final String sql = "SELECT * FROM table";

        // When
        final PreparedSql result = new PreparedSql(sql);

        // Then
        assertEquals(sql, result.sql());
        assertTrue(result.bindValues().isEmpty());
    }

    @Test
    void preparedSql_bindValues() {
        // Given
        final List<BindValue> bindValues = List.of(new BindValue("test", Types.VARCHAR));
        final String sql = "SELECT * FROM table WHERE column = ?";

        // When
        final PreparedSql result = new PreparedSql(sql, bindValues);

        // Then
        assertEquals(sql, result.sql());
        assertEquals(bindValues, result.bindValues());
    }

}