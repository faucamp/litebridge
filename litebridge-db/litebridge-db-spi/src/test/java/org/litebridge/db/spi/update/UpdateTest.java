package org.litebridge.db.spi.update;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.query.Condition;
import org.litebridge.db.spi.query.Operator;

import java.sql.Types;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UpdateTest {

    @Test
    void constructor() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final ColumnMetaData column = new ColumnMetaData(table, "TEST_COLUMN", true, Types.VARCHAR);
        final ColumnValue columnValue = new ColumnValue(column.toColumn(), "testValue");
        final Condition condition = new Condition(new Column(table, "ID"), Operator.EQ, 1L);

        // When
        final Update result = new Update(table, List.of(columnValue), List.of(condition));

        // Then
        assertEquals(table, result.table());
        assertEquals(List.of(columnValue), result.columnValues());
        assertEquals(List.of(condition), result.where());
    }
}