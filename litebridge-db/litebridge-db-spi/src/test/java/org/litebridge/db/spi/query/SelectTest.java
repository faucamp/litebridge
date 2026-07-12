package org.litebridge.db.spi.query;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.expression.ColumnExpressionImpl;
import org.litebridge.db.spi.expression.ColumnExpressionTest;
import org.litebridge.db.spi.expression.DelegateExpression;
import org.litebridge.db.spi.expression.LiteralExpression;
import org.litebridge.db.spi.expression.SelectExpression;

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
        final Condition condition = new Condition(ColumnExpressionTest.select(column), operator, new LiteralExpression(value));
        final ConditionGroup conditionGroup = new ConditionGroup(new LogicCondition(LogicOperator.AND, condition));
        final Join join = new Join(table, conditionGroup);
        final List<SelectExpression> groupBy = List.of(new ColumnExpressionTest.SelectColumnExpression(column));
        final OrderBy orderBy = new OrderBy(new ColumnExpressionTest.SelectColumnExpression(column), true);
        final Limit limit = new Limit(Optional.of(10), Optional.of(20));

        // When
        final Select result = new Select(
                table,
                List.of(new TestColumnExpression(column)),
                List.of(join),
                Optional.of(conditionGroup),
                groupBy,
                Optional.empty(),
                List.of(orderBy),
                Optional.of(limit)
        );

        // Then
        assertEquals(table, result.table());
        assertEquals(1, result.expressions().size());
        assertInstanceOf(TestColumnExpression.class, result.expressions().getFirst());
        assertEquals(column, ((TestColumnExpression) result.expressions().getFirst()).column());
        assertEquals(List.of(join), result.joins());
        assertEquals(List.of(orderBy), result.orderBy());
        assertEquals(conditionGroup, result.where().orElseThrow());
        assertEquals(Optional.of(limit), result.limit());
    }

    @NullMarked
    private static final class TestColumnExpression extends ColumnExpressionImpl {

        public TestColumnExpression(final Column column) {
            super(column);
        }

        @Override
        public String toSql(final Operation operation, final ClauseType context, final @Nullable DelegateExpression parent) {
            return column.name();
        }
    }
}