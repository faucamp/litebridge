package org.litebridge.orm.engine.ast;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.query.Join;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JoinNodeTest {

    @Test
    void constructorAndGetters() {
        // Given
        final QueryNode previous = new DeleteNode(null, "p1", null);
        final Join.JoinType type = Join.JoinType.INNER;
        final Class<?> dtoClass = String.class;
        final String rightTable = "USERS";

        // When
        final JoinNode node = new JoinNode(previous, type, dtoClass, null, rightTable, null, null);

        // Then
        assertSame(previous, node.previous());
        assertEquals(type, node.type());
        assertEquals(dtoClass, node.dtoClass());
        assertEquals(rightTable, node.table());
        assertNull(node.condition());
    }

    @Test
    void setConditionUpdatesCondition() {
        // Given
        final JoinNode node = new JoinNode(null, Join.JoinType.LEFT, null, null, "ORDERS", null, null);
        final QueryNode condition = new ConditionWithIdNode(null, LogicOperator.AND, Operator.EQ, 1);

        // When
        node.setCondition(condition);

        // Then
        assertSame(condition, node.condition());
    }

    @Test
    void equals_hashCode_toString() {
        // Given
        final QueryNode prev1 = new DeleteNode(null, "p1", null);
        final QueryNode prev2 = new DeleteNode(null, "p2", null);
        final QueryNode cond1 = new ConditionWithIdNode(null, LogicOperator.AND, Operator.EQ, 1);
        final QueryNode cond2 = new ConditionWithIdNode(null, LogicOperator.OR, Operator.EQ, 1);

        final JoinNode node1 = new JoinNode(prev1, Join.JoinType.INNER, String.class, null, "USERS", null, null);
        node1.setCondition(cond1);
        final JoinNode node2 = new JoinNode(prev1, Join.JoinType.INNER, String.class, null, "USERS", null, null);
        node2.setCondition(cond1);

        final JoinNode diffPrev = new JoinNode(prev2, Join.JoinType.INNER, String.class, null, "USERS", null, null);
        diffPrev.setCondition(cond1);

        final JoinNode diffType = new JoinNode(prev1, Join.JoinType.LEFT, String.class, null, "USERS", null, null);
        diffType.setCondition(cond1);

        final JoinNode diffDto = new JoinNode(prev1, Join.JoinType.INNER, Integer.class, null, "USERS", null, null);
        diffDto.setCondition(cond1);

        final JoinNode diffTable = new JoinNode(prev1, Join.JoinType.INNER, String.class, null, "ORDERS", null, null);
        diffTable.setCondition(cond1);

        final JoinNode diffCond = new JoinNode(prev1, Join.JoinType.INNER, String.class, null, "USERS", null, null);
        diffCond.setCondition(cond2);

        // When / Then
        assertEquals(node1, node1);
        assertEquals(node1, node2);
        assertEquals(node1.hashCode(), node2.hashCode());

        assertNotEquals(node1, diffPrev);
        assertNotEquals(node1, diffType);
        assertNotEquals(node1, diffDto);
        assertNotEquals(node1, diffTable);
        assertNotEquals(node1, diffCond);

        assertNotEquals(node1, null);
        assertNotEquals(node1, "other");

        final String str = node1.toString();
        assertTrue(str.contains("JoinNode["));
    }
}
