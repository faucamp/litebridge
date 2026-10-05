package org.litebridge.orm.engine.ast;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WhereNodeTest {

    @Test
    void constructorAndGetters() {
        // Given
        final QueryNode previous = new DeleteNode(null, "p1", null);
        final QueryNode condition = new ConditionWithIdNode(null, LogicOperator.AND, Operator.EQ, 1);

        // When
        final WhereNode node = new WhereNode(previous, condition);

        // Then
        assertSame(previous, node.previous());
        assertSame(condition, node.condition());
    }

    @Test
    void withConditionUpdatesConditionAndReturnsThis() {
        // Given
        final QueryNode previous = new DeleteNode(null, "p1", null);
        final QueryNode condition1 = new ConditionWithIdNode(null, LogicOperator.AND, Operator.EQ, 1);
        final QueryNode condition2 = new ConditionWithIdNode(null, LogicOperator.OR, Operator.EQ, 2);

        final WhereNode node = new WhereNode(previous, condition1);

        // When
        final WhereNode returned = node.withCondition(condition2);

        // Then
        assertSame(node, returned);
        assertSame(condition2, node.condition());
    }

    @Test
    void equals_hashCode_toString() {
        // Given
        final QueryNode prev1 = new DeleteNode(null, "p1", null);
        final QueryNode prev2 = new DeleteNode(null, "p2", null);
        final QueryNode cond1 = new ConditionWithIdNode(null, LogicOperator.AND, Operator.EQ, 1);
        final QueryNode cond2 = new ConditionWithIdNode(null, LogicOperator.OR, Operator.EQ, 1);

        final WhereNode node1 = new WhereNode(prev1, cond1);
        final WhereNode node2 = new WhereNode(prev1, cond1);

        final WhereNode diffPrev = new WhereNode(prev2, cond1);
        final WhereNode diffCond = new WhereNode(prev1, cond2);

        // When / Then
        assertEquals(node1, node1);
        assertEquals(node1, node2);
        assertEquals(node1.hashCode(), node2.hashCode());

        assertNotEquals(node1, diffPrev);
        assertNotEquals(node1, diffCond);
        assertNotEquals(node1, null);
        assertNotEquals(node1, "other");

        final String str = node1.toString();
        assertTrue(str.contains("WhereNode["));
        assertTrue(str.contains("previous="));
        assertTrue(str.contains("condition="));
    }
}
