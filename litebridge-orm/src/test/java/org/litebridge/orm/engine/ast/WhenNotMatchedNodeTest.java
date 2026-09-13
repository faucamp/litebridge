package org.litebridge.orm.engine.ast;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class WhenNotMatchedNodeTest {

    @Test
    void recordComponentsAndGetters() {
        // Given
        final QueryNode previous = new DeleteNode(null, "p1", null);
        final QueryNode and = new DeleteNode(null, "a1", null);
        final QueryNode insert = new DeleteNode(null, "i1", null);

        // When
        final WhenNotMatchedNode node = new WhenNotMatchedNode(previous, and, insert);

        // Then
        assertSame(previous, node.previous());
        assertSame(and, node.and());
        assertSame(insert, node.insert());
    }

    @Test
    void equals_hashCode() {
        // Given
        final QueryNode prev1 = new DeleteNode(null, "p1", null);
        final QueryNode prev2 = new DeleteNode(null, "p2", null);
        final QueryNode and1 = new DeleteNode(null, "a1", null);
        final QueryNode and2 = new DeleteNode(null, "a2", null);
        final QueryNode ins1 = new DeleteNode(null, "i1", null);
        final QueryNode ins2 = new DeleteNode(null, "i2", null);

        final WhenNotMatchedNode node1 = new WhenNotMatchedNode(prev1, and1, ins1);
        final WhenNotMatchedNode node2 = new WhenNotMatchedNode(prev1, and1, ins1);

        final WhenNotMatchedNode diffPrev = new WhenNotMatchedNode(prev2, and1, ins1);
        final WhenNotMatchedNode diffAnd = new WhenNotMatchedNode(prev1, and2, ins1);
        final WhenNotMatchedNode diffIns = new WhenNotMatchedNode(prev1, and1, ins2);
        final WhenNotMatchedNode nullAnd = new WhenNotMatchedNode(prev1, null, ins1);

        // When / Then
        assertEquals(node1, node1);
        assertEquals(node1, node2);
        assertEquals(node1.hashCode(), node2.hashCode());

        assertNotEquals(node1, diffPrev);
        assertNotEquals(node1, diffAnd);
        assertNotEquals(node1, diffIns);
        assertNotEquals(node1, nullAnd);
        assertNull(nullAnd.and());

        assertNotEquals(node1, null);
        assertNotEquals(node1, "other");
    }
}
