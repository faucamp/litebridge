package org.litebridge.db.spi.query;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.expression.SelectExpression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.mock;

class LogicConditionTest {

    @Test
    void constructor_3args() {
        // Given
        final SelectExpression lhs = mock(SelectExpression.class);
        final SelectExpression rhs = mock(SelectExpression.class);

        // When
        final LogicCondition logicCondition = new LogicCondition(lhs, Operator.EQ, rhs);

        // Then
        assertEquals(LogicOperator.NOOP, logicCondition.logicOperator());
        assertEquals(lhs, logicCondition.condition().lhs());
        assertEquals(Operator.EQ, logicCondition.condition().operator());
        assertEquals(rhs, logicCondition.condition().rhs());
    }

    @Test
    void constructor_2args_andEquals() {
        // Given
        final SelectExpression lhs = mock(SelectExpression.class);
        final Condition condition = new Condition(lhs, Operator.GT, null);
        final LogicCondition lc1 = new LogicCondition(LogicOperator.AND, condition);
        final LogicCondition lc2 = new LogicCondition(LogicOperator.AND, condition);
        final LogicCondition lcDiff = new LogicCondition(LogicOperator.OR, condition);

        // When & Then
        assertEquals(LogicOperator.AND, lc1.logicOperator());
        assertEquals(condition, lc1.condition());
        assertEquals(lc1, lc2);
        assertEquals(lc1.hashCode(), lc2.hashCode());
        assertNotEquals(lc1, lcDiff);
    }
}
