package org.litebridge.orm.engine.ast;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.math.MathOperator;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class SetNodeTest {

    @Test
    void constructorWithColumnAndGetters() {
        // Given
        final QueryNode previous = new DeleteNode(null, "p1", null);
        final String column = "status";
        final Object value = "PENDING";

        // When
        final SetNode node = new SetNode(previous, column, value);

        // Then
        assertSame(previous, node.previous());
        assertEquals(column, node.column());
        assertNull(node.expressionSpec());
        assertEquals(value, node.value());
        assertNull(node.mathOperator());
    }

    @Test
    void constructorWithExpressionSpecAndGetters() {
        // Given
        final QueryNode previous = new DeleteNode(null, "p1", null);
        final ExpressionSpec expressionSpec = new SelectColumnSpec(new Column(new Table("t"), "col1"));
        final Object value = 123;

        // When
        final SetNode node = new SetNode(previous, expressionSpec, value);

        // Then
        assertSame(previous, node.previous());
        assertNull(node.column());
        assertSame(expressionSpec, node.expressionSpec());
        assertEquals(value, node.value());
        assertNull(node.mathOperator());
    }

    @Test
    void fullConstructorAndGetters() {
        // Given
        final QueryNode previous = new DeleteNode(null, "p1", null);
        final String column = "count";
        final ExpressionSpec expressionSpec = new SelectColumnSpec(new Column(new Table("t"), "col1"));
        final Object value = 1;
        final MathOperator mathOperator = MathOperator.ADD;

        // When
        final SetNode node = new SetNode(previous, column, expressionSpec, value, mathOperator);

        // Then
        assertSame(previous, node.previous());
        assertEquals(column, node.column());
        assertSame(expressionSpec, node.expressionSpec());
        assertEquals(value, node.value());
        assertEquals(mathOperator, node.mathOperator());
    }

    @Test
    void equals_hashCode() {
        // Given
        final QueryNode prev1 = new DeleteNode(null, "p1", null);
        final QueryNode prev2 = new DeleteNode(null, "p2", null);
        final ExpressionSpec expr1 = new SelectColumnSpec(new Column(new Table("t"), "col1"));
        final ExpressionSpec expr2 = new SelectColumnSpec(new Column(new Table("t"), "col2"));

        final SetNode base = new SetNode(prev1, "col1", expr1, "val1", MathOperator.ADD);
        final SetNode same = new SetNode(prev1, "col1", expr1, "val1", MathOperator.ADD);

        final SetNode diffPrev = new SetNode(prev2, "col1", expr1, "val1", MathOperator.ADD);
        final SetNode diffCol = new SetNode(prev1, "col2", expr1, "val1", MathOperator.ADD);
        final SetNode diffExpr = new SetNode(prev1, "col1", expr2, "val1", MathOperator.ADD);
        final SetNode diffMath = new SetNode(prev1, "col1", expr1, "val1", MathOperator.SUBTRACT);

        // Value is not part of equals/hashCode in SetNode
        final SetNode diffValue = new SetNode(prev1, "col1", expr1, "val2", MathOperator.ADD);

        // When / Then
        assertEquals(base, base);
        assertEquals(base, same);
        assertEquals(base.hashCode(), same.hashCode());

        assertEquals(base, diffValue);
        assertEquals(base.hashCode(), diffValue.hashCode());

        assertNotEquals(base, diffPrev);
        assertNotEquals(base, diffCol);
        assertNotEquals(base, diffExpr);
        assertNotEquals(base, diffMath);

        assertNotEquals(base, null);
        assertNotEquals(base, "other");
    }
}
