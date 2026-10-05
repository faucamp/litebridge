package org.litebridge.db.spi.query;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.expression.ColumnTestExpression;
import org.litebridge.db.spi.expression.LiteralTestExpression;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JoinTest {

    @Test
    void constructor() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final Column column = new Column(table, "TEST_COLUMN");
        final Operator operator = Operator.EQ;
        final Object value = "testValue";
        final Condition condition = new Condition(new ColumnTestExpression(column), operator, new LiteralTestExpression(value));
        final ConditionGroup conditionGroup = new ConditionGroup(new LogicCondition(LogicOperator.AND, condition));

        // When
        final Join result = new Join(Join.JoinType.INNER, table, conditionGroup);

        // Then
        assertEquals(table, result.target());
        assertEquals(conditionGroup, result.conditions());
    }
}