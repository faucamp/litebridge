package org.litebridge.orm.engine.ast;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class UpdateNodeTest {

    @Test
    void recordComponentsAndGetters() {
        // Given
        final String table = "USERS";
        final Class<?> dtoClass = String.class;

        // When
        final UpdateNode node = new UpdateNode(dtoClass, null, table);

        // Then
        assertNull(node.previous());
        assertEquals(table, node.table());
        assertEquals(dtoClass, node.dtoClass());
    }

    @Test
    void equals_hashCode() {
        // Given
        final UpdateNode node1 = new UpdateNode("USERS");
        final UpdateNode node2 = new UpdateNode("USERS");

        // When / Then
        assertEquals(node1, node2);
        assertEquals(node1.hashCode(), node2.hashCode());
    }
}
