package org.litebridge.db.spi.sql;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.query.TypeConversionMetaData;

import java.sql.Types;
import java.util.Collections;
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
        final String sql = "SELECT * FROM table WHERE expression = ?";
        final TypeConversionMetaData typeConversionMetaData = new TypeConversionMetaData(Collections.emptyMap(), new Class<?>[0]);

        // When
        final PreparedSql result = new PreparedSql(sql, bindValues, typeConversionMetaData);

        // Then
        assertEquals(sql, result.sql());
        assertEquals(bindValues, result.bindValues());
    }
}