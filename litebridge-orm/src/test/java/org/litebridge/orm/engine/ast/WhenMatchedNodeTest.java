package org.litebridge.orm.engine.ast;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class WhenMatchedNodeTest {

    @Test
    void recordComponentsAndGetters() {
        // Given
        final QueryNode previous = new DeleteNode(null, "p1", null);
        final QueryNode update = new DeleteNode(null, "u1", null);

        // When
        final WhenMatchedNode node = new WhenMatchedNode(previous, update);

        // Then
        assertSame(previous, node.previous());
        assertSame(update, node.update());
    }

    @Test
    void equals_hashCode() {
        // Given
        final QueryNode prev1 = new DeleteNode(null, "p1", null);
        final QueryNode prev2 = new DeleteNode(null, "p2", null);
        final QueryNode update1 = new DeleteNode(null, "u1", null);
        final QueryNode update2 = new DeleteNode(null, "u2", null);

        final WhenMatchedNode node1 = new WhenMatchedNode(prev1, update1);
        final WhenMatchedNode node2 = new WhenMatchedNode(prev1, update1);

        final WhenMatchedNode diffPrev = new WhenMatchedNode(prev2, update1);
        final WhenMatchedNode diffUpdate = new WhenMatchedNode(prev1, update2);

        // When / Then
        assertEquals(node1, node1);
        assertEquals(node1, node2);
        assertEquals(node1.hashCode(), node2.hashCode());

        assertNotEquals(node1, diffPrev);
        assertNotEquals(node1, diffUpdate);
        assertNotEquals(node1, null);
        assertNotEquals(node1, "other");
    }
}
