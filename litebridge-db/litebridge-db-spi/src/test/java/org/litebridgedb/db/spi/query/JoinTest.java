package org.litebridgedb.db.spi.query;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.expression.ColumnExpressionTest;
import org.litebridgedb.db.spi.expression.LiteralExpression;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JoinTest {

    @Test
    void constructor() {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        final Column column = new Column(table, "TEST_COLUMN");
        final Operator operator = Operator.EQ;
        final Object value = "testValue";
        final Condition condition = new Condition(ColumnExpressionTest.select(column), operator, new LiteralExpression(value));
        final ConditionGroup conditionGroup = new ConditionGroup(new LogicCondition(LogicOperator.AND, condition));

        // When
        final Join result = new Join(table, conditionGroup);

        // Then
        assertEquals(table, result.table());
        assertEquals(conditionGroup, result.conditions());
    }
}