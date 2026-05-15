package org.litebridgedb.db.spi.update;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Table;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ColumnValueTest {

    @Test
    void constructor() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final Column column = new Column(table, "TEST_COLUMN");
        final Object value = "testValue";

        // When
        final ColumnValue columnValue = new ColumnValue(column, value);

        // Then
        assertEquals(column, columnValue.column());
        assertEquals(value, columnValue.value());
    }
}