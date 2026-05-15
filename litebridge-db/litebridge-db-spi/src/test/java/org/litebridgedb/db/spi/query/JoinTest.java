package org.litebridgedb.db.spi.query;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Table;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JoinTest {

    @Test
    void constructor() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final Column column = new Column(table, "TEST_COLUMN");
        final Operator operator = Operator.EQ;
        final Object value = "testValue";
        final Condition condition = new Condition(column, operator, value);

        // When
        final Join result = new Join(table, List.of(condition));

        // Then
        assertEquals(table, result.table());
        assertEquals(List.of(condition), result.conditions());
    }
}