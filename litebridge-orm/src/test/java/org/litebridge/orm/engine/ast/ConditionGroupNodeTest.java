package org.litebridge.orm.engine.ast;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.query.LogicOperator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ConditionGroupNodeTest {

    @Test
    void recordComponentsAndGetters() {
        // Given
        final QueryNode previous = new DeleteNode(null, "t1", null);
        final QueryNode lastChild = new DeleteNode(null, "t2", null);
        final LogicOperator operator = LogicOperator.AND;

        // When
        final ConditionGroupNode node = new ConditionGroupNode(previous, operator, lastChild);

        // Then
        assertSame(previous, node.previous());
        assertEquals(operator, node.logicOperator());
        assertSame(lastChild, node.lastChild());
    }

    @Test
    void equals_hashCode() {
        // Given
        final QueryNode previous1 = new DeleteNode(null, "p1", null);
        final QueryNode previous2 = new DeleteNode(null, "p2", null);
        final QueryNode child1 = new DeleteNode(null, "c1", null);
        final QueryNode child2 = new DeleteNode(null, "c2", null);

        final ConditionGroupNode node1 = new ConditionGroupNode(previous1, LogicOperator.AND, child1);
        final ConditionGroupNode node2 = new ConditionGroupNode(previous1, LogicOperator.AND, child1);
        final ConditionGroupNode nodeDiffPrev = new ConditionGroupNode(previous2, LogicOperator.AND, child1);
        final ConditionGroupNode nodeDiffOp = new ConditionGroupNode(previous1, LogicOperator.OR, child1);
        final ConditionGroupNode nodeDiffChild = new ConditionGroupNode(previous1, LogicOperator.AND, child2);

        // When / Then
        assertEquals(node1, node1);
        assertEquals(node1, node2);
        assertEquals(node1.hashCode(), node2.hashCode());

        assertNotEquals(node1, nodeDiffPrev);
        assertNotEquals(node1, nodeDiffOp);
        assertNotEquals(node1, nodeDiffChild);
        assertNotEquals(node1, null);
        assertNotEquals(node1, "other");
    }
}
