package org.litebridge.orm.engine.ast;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ConditionJoinUsingNodeTest {

    @Test
    void recordComponentsAndGetters() {
        // Given
        final QueryNode previous = new DeleteNode(null, "p1", null);
        final LogicOperator operator = LogicOperator.AND;
        final String column = "tenant_id";
        final ExpressionSpec expression = new SelectColumnSpec(new Column(new Table("t"), "col1"));

        // When
        final ConditionJoinUsingNode node = new ConditionJoinUsingNode(previous, operator, column, expression);

        // Then
        assertSame(previous, node.previous());
        assertEquals(operator, node.logicOperator());
        assertEquals(column, node.usingColumn());
        assertSame(expression, node.usingExpression());
    }

    @Test
    void equals_hashCode() {
        // Given
        final QueryNode prev1 = new DeleteNode(null, "p1", null);
        final QueryNode prev2 = new DeleteNode(null, "p2", null);
        final ExpressionSpec expr1 = new SelectColumnSpec(new Column(new Table("t"), "col1"));
        final ExpressionSpec expr2 = new SelectColumnSpec(new Column(new Table("t"), "col2"));

        final ConditionJoinUsingNode node1 = new ConditionJoinUsingNode(prev1, LogicOperator.AND, "col1", expr1);
        final ConditionJoinUsingNode node2 = new ConditionJoinUsingNode(prev1, LogicOperator.AND, "col1", expr1);

        final ConditionJoinUsingNode diffPrev = new ConditionJoinUsingNode(prev2, LogicOperator.AND, "col1", expr1);
        final ConditionJoinUsingNode diffOp = new ConditionJoinUsingNode(prev1, LogicOperator.OR, "col1", expr1);
        final ConditionJoinUsingNode diffCol = new ConditionJoinUsingNode(prev1, LogicOperator.AND, "col2", expr1);
        final ConditionJoinUsingNode diffExpr = new ConditionJoinUsingNode(prev1, LogicOperator.AND, "col1", expr2);

        // When / Then
        assertEquals(node1, node1);
        assertEquals(node1, node2);
        assertEquals(node1.hashCode(), node2.hashCode());

        assertNotEquals(node1, diffPrev);
        assertNotEquals(node1, diffOp);
        assertNotEquals(node1, diffCol);
        assertNotEquals(node1, diffExpr);
        assertNotEquals(node1, null);
        assertNotEquals(node1, "other");
    }
}
