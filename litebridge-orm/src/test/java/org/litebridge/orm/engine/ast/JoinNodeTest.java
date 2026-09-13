package org.litebridge.orm.engine.ast;

import org.junit.jupiter.api.Test;
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
        final String type = "INNER";
        final Class<?> dtoClass = String.class;
        final String rightTable = "USERS";

        // When
        final JoinNode node = new JoinNode(previous, type, dtoClass, rightTable);

        // Then
        assertSame(previous, node.previous());
        assertEquals(type, node.type());
        assertEquals(dtoClass, node.dtoClass());
        assertEquals(rightTable, node.rightTable());
        assertNull(node.condition());
    }

    @Test
    void setConditionUpdatesCondition() {
        // Given
        final JoinNode node = new JoinNode(null, "LEFT", null, "ORDERS");
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

        final JoinNode node1 = new JoinNode(prev1, "INNER", String.class, "USERS");
        node1.setCondition(cond1);
        final JoinNode node2 = new JoinNode(prev1, "INNER", String.class, "USERS");
        node2.setCondition(cond1);

        final JoinNode diffPrev = new JoinNode(prev2, "INNER", String.class, "USERS");
        diffPrev.setCondition(cond1);

        final JoinNode diffType = new JoinNode(prev1, "LEFT", String.class, "USERS");
        diffType.setCondition(cond1);

        final JoinNode diffDto = new JoinNode(prev1, "INNER", Integer.class, "USERS");
        diffDto.setCondition(cond1);

        final JoinNode diffTable = new JoinNode(prev1, "INNER", String.class, "ORDERS");
        diffTable.setCondition(cond1);

        final JoinNode diffCond = new JoinNode(prev1, "INNER", String.class, "USERS");
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
        assertTrue(str.contains("type=INNER"));
        assertTrue(str.contains("rightTable=USERS"));
    }
}
