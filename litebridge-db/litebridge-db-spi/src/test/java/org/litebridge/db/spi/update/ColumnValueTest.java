package org.litebridge.db.spi.update;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Table;

import java.sql.Types;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ColumnValueTest {

    @Test
    void constructor() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnMetaData column = new ColumnMetaData(table, "TEST_COLUMN", true, Types.VARCHAR);
        final Object value = "testValue";

        // When
        final ColumnValue columnValue = new ColumnValue(column, value);

        // Then
        assertEquals(column, columnValue.column());
        assertEquals(value, columnValue.value());
    }
}