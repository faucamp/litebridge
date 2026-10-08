package org.litebridge.orm.api.select.model;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.function.aggregate.CountSpec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ConditionSpecTest {

    @Test
    void testConditionSpec() {
        // Given
        final ExpressionSpec expressionSpec = new CountSpec();
        final ConditionSpec conditionSpec = new ConditionSpec("lhs", expressionSpec, Operator.EQ, "abc");

        // Then
        assertEquals("lhs", conditionSpec.lhsColumn());
        assertSame(expressionSpec, conditionSpec.lhsExpression());
        assertEquals(Operator.EQ, conditionSpec.operator());
        assertEquals("abc", conditionSpec.value());
    }

}