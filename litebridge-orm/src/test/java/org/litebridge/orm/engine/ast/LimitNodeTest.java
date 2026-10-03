package org.litebridge.orm.engine.ast;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class LimitNodeTest {

    @Test
    void recordComponentsAndGetters() {
        // Given
        final QueryNode previous = new DeleteNode(null, "p1", null);
        final Integer limit = 10;
        final Integer offset = 20;

        // When
        final LimitNode node = new LimitNode(previous, limit, offset);

        // Then
        assertSame(previous, node.previous());
        assertEquals(limit, node.limit());
        assertEquals(offset, node.offset());
    }

    @Test
    void equals_hashCode() {
        // Given
        final QueryNode prev1 = new DeleteNode(null, "p1", null);
        final QueryNode prev2 = new DeleteNode(null, "p2", null);

        final LimitNode node1 = new LimitNode(prev1, 10, 20);
        final LimitNode node2 = new LimitNode(prev1, 10, 20);

        final LimitNode diffPrev = new LimitNode(prev2, 10, 20);
        final LimitNode diffLimit = new LimitNode(prev1, 15, 20);
        final LimitNode diffOffset = new LimitNode(prev1, 10, 25);

        // When / Then
        assertEquals(node1, node1);
        assertEquals(node1, node2);
        assertEquals(node1.hashCode(), node2.hashCode());

        assertNotEquals(node1, diffPrev);
        assertNotEquals(node1, diffLimit);
        assertNotEquals(node1, diffOffset);
        assertNotEquals(node1, null);
        assertNotEquals(node1, "other");
    }
}
