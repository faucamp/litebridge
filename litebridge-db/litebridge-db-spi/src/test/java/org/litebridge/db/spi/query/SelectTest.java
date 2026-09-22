package org.litebridge.db.spi.query;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.expression.ColumnTestExpression;
import org.litebridge.db.spi.expression.LiteralTestExpression;
import org.litebridge.db.spi.expression.SelectExpression;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

class SelectTest {

    @Test
    void constructor() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final Column column = new Column(table, "TEST_COLUMN");
        final Operator operator = Operator.EQ;
        final Object value = "testValue";
        final Condition condition = new Condition(new ColumnTestExpression(column), operator, new LiteralTestExpression(value));
        final ConditionGroup conditionGroup = new ConditionGroup(new LogicCondition(LogicOperator.AND, condition));
        final Join join = new Join(Join.JoinType.INNER, table, conditionGroup);
        final List<SelectExpression> groupBy = List.of(new ColumnTestExpression(column));
        final OrderBy orderBy = new OrderBy(new ColumnTestExpression(column), true);
        final Limit limit = new Limit(10, 20);

        // When
        final Select result = new Select(
                table,
                List.of(new ColumnTestExpression(column)),
                List.of(join),
                conditionGroup,
                groupBy,
                null,
                List.of(orderBy),
                limit
        );

        // Then
        assertEquals(table, result.table());
        assertEquals(1, result.expressions().size());
        assertInstanceOf(ColumnTestExpression.class, result.expressions().getFirst());
        assertEquals(column, ((ColumnTestExpression) result.expressions().getFirst()).column());
        assertEquals(List.of(join), result.joins());
        assertEquals(conditionGroup, result.where());
        assertNull(result.having());
        assertEquals(List.of(orderBy), result.orderBy());
        assertEquals(limit, result.limit());
    }
}