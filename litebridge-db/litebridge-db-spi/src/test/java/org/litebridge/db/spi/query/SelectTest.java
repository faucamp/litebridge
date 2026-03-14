package org.litebridge.db.spi.query;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SelectTest {

    @Test
    void constructor() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final Column column = new Column(table, "TEST_COLUMN");
        final Operator operator = Operator.EQ;
        final Object value = "testValue";
        final Condition condition = new Condition(column, operator, value);
        final Join join = new Join(table, List.of(condition));
        final OrderBy orderBy = new OrderBy(column, true);
        final Limit limit = new Limit(Optional.of(10), Optional.of(20));

        // When
        final Select result = new Select(
                table,
                List.of(column),
                List.of(join),
                List.of(orderBy),
                List.of(condition),
                Optional.of(limit)
        );

        // Then
        assertEquals(table, result.table());
        assertEquals(List.of(column), result.columns());
        assertEquals(List.of(join), result.joins());
        assertEquals(List.of(orderBy), result.orderBy());
        assertEquals(List.of(condition), result.where());
        assertEquals(Optional.of(limit), result.limit());
    }
}