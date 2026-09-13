package org.litebridge.orm.engine.ast;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MergeNodeTest {

    @Test
    void recordComponentsAndGetters() {
        // Given
        final String table = "TARGET";
        final Class<?> dtoClass = String.class;

        // When
        final MergeNode node = new MergeNode(table, dtoClass);

        // Then
        assertNull(node.previous());
        assertEquals(table, node.table());
        assertEquals(dtoClass, node.dtoClass());
    }

    @Test
    void equals_hashCode() {
        // Given
        final MergeNode node1 = new MergeNode("TARGET", String.class);
        final MergeNode node2 = new MergeNode("TARGET", String.class);

        final MergeNode diffTable = new MergeNode("OTHER", String.class);
        final MergeNode diffDto = new MergeNode("TARGET", Integer.class);

        // When / Then
        assertEquals(node1, node1);
        assertEquals(node1, node2);
        assertEquals(node1.hashCode(), node2.hashCode());

        assertNotEquals(node1, diffTable);
        assertNotEquals(node1, diffDto);
        assertNotEquals(node1, null);
        assertNotEquals(node1, "other");
    }
}
