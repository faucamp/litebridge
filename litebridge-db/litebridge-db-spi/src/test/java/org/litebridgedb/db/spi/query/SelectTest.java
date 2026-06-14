package org.litebridgedb.db.spi.query;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Operation;
import org.litebridgedb.db.spi.Table;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

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
                List.of(new TestColumnExpression(column)),
                List.of(join),
                List.of(orderBy),
                List.of(condition),
                Optional.of(limit)
        );

        // Then
        assertEquals(table, result.table());
        assertEquals(1, result.expressions().size());
        assertInstanceOf(TestColumnExpression.class, result.expressions().getFirst());
        assertEquals(column, ((TestColumnExpression) result.expressions().getFirst()).column());
        assertEquals(List.of(join), result.joins());
        assertEquals(List.of(orderBy), result.orderBy());
        assertEquals(List.of(condition), result.where());
        assertEquals(Optional.of(limit), result.limit());
    }

    private final class TestColumnExpression extends ColumnExpression {

        public TestColumnExpression(final Column column) {
            super(column);
        }

        @Override
        public String toSql(final Operation operation) {
            return column.name();
        }
    }
}