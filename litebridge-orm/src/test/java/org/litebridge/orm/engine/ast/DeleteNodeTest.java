package org.litebridge.orm.engine.ast;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class DeleteNodeTest {

    @Test
    void recordComponentsAndGetters() {
        // Given
        final QueryNode previous = new DeleteNode(null, "PARENT", null);
        final String table = "USERS";
        final Class<?> dtoClass = String.class;

        // When
        final DeleteNode node = new DeleteNode(previous, table, dtoClass);

        // Then
        assertSame(previous, node.previous());
        assertEquals(table, node.table());
        assertEquals(dtoClass, node.dtoClass());
    }

    @Test
    void equals_hashCode() {
        // Given
        final QueryNode prev1 = new DeleteNode(null, "p1", null);
        final QueryNode prev2 = new DeleteNode(null, "p2", null);

        final DeleteNode node1 = new DeleteNode(prev1, "USERS", String.class);
        final DeleteNode node2 = new DeleteNode(prev1, "USERS", String.class);

        final DeleteNode diffPrev = new DeleteNode(prev2, "USERS", String.class);
        final DeleteNode diffTable = new DeleteNode(prev1, "ORDERS", String.class);
        final DeleteNode diffDto = new DeleteNode(prev1, "USERS", Integer.class);
        final DeleteNode nullDto = new DeleteNode(null, "USERS", null);

        // When / Then
        assertEquals(node1, node1);
        assertEquals(node1, node2);
        assertEquals(node1.hashCode(), node2.hashCode());

        assertNotEquals(node1, diffPrev);
        assertNotEquals(node1, diffTable);
        assertNotEquals(node1, diffDto);
        assertNotEquals(node1, nullDto);
        assertNull(nullDto.previous());

        assertNotEquals(node1, null);
        assertNotEquals(node1, "other");
    }
}
