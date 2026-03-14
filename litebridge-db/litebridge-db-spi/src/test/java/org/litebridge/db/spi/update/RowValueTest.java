package org.litebridge.db.spi.update;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Table;

import java.sql.Types;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RowValueTest {

    @Test
    void constructor() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnMetaData column = new ColumnMetaData(table, "TEST_COLUMN", true, Types.VARCHAR);
        final ColumnValue columnValue = new ColumnValue(column, "testValue");

        // When
        final RowValue rowValue = new RowValue(List.of(columnValue));

        // Then
        assertEquals(List.of(columnValue), rowValue.columns());
    }
}