package org.litebridge.orm.engine.ast;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class UpdateNodeTest {

    @Test
    void recordComponentsAndGetters() {
        // Given
        final QueryNode previous = new DeleteNode(null, "PARENT", null);
        final String table = "USERS";
        final Class<?> dtoClass = String.class;

        // When
        final UpdateNode node = new UpdateNode(previous, table, dtoClass);

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

        final UpdateNode node1 = new UpdateNode(prev1, "USERS", String.class);
        final UpdateNode node2 = new UpdateNode(prev1, "USERS", String.class);

        final UpdateNode diffPrev = new UpdateNode(prev2, "USERS", String.class);
        final UpdateNode diffTable = new UpdateNode(prev1, "ORDERS", String.class);
        final UpdateNode diffDto = new UpdateNode(prev1, "USERS", Integer.class);
        final UpdateNode nullPrev = new UpdateNode(null, "USERS", null);

        // When / Then
        assertEquals(node1, node1);
        assertEquals(node1, node2);
        assertEquals(node1.hashCode(), node2.hashCode());

        assertNotEquals(node1, diffPrev);
        assertNotEquals(node1, diffTable);
        assertNotEquals(node1, diffDto);
        assertNotEquals(node1, nullPrev);
        assertNull(nullPrev.previous());

        assertNotEquals(node1, null);
        assertNotEquals(node1, "other");
    }
}
