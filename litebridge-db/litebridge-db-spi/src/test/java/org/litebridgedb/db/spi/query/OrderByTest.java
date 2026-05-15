package org.litebridgedb.db.spi.query;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Table;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderByTest {

    @Test
    void constructor() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final Column column = new Column(table, "TEST_COLUMN");
        final boolean ascending = false;

        // When
        final OrderBy orderBy = new OrderBy(column, ascending);

        // Then
        assertEquals(column, orderBy.column());
        assertEquals(ascending, orderBy.asc());
    }
}