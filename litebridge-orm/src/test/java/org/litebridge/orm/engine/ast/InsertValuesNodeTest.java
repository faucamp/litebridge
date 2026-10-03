package org.litebridge.orm.engine.ast;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class InsertValuesNodeTest {

    @Test
    void recordComponentsAndGetters() {
        // Given
        final InsertNode insertNode = new InsertNode("TEST_TABLE", null, new String[]{"COL1", "COL2"});
        final Object[] values = new Object[]{"val1", 42};

        // When
        final InsertValuesNode node = new InsertValuesNode(insertNode, values);

        // Then
        assertSame(insertNode, node.previous());
        assertArrayEquals(values, node.values());
    }

    @Test
    void equals_hashCode() {
        // Given
        final InsertNode insertNode = new InsertNode("TEST_TABLE", null, new String[]{"COL1", "COL2"});
        final InsertNode otherInsertNode = new InsertNode("OTHER_TABLE", null, new String[]{"COL1", "COL2"});

        final InsertValuesNode insertValuesNode = new InsertValuesNode(insertNode, new Object[]{"val1", "val2"});
        final InsertValuesNode insertValuesNode2 = new InsertValuesNode(insertNode, new Object[]{"val1", "val2"});
        final InsertValuesNode insertValuesNode3 = new InsertValuesNode(insertNode, new Object[]{"val2", "val3"});
        final InsertValuesNode insertValuesNodeDiffPrev = new InsertValuesNode(otherInsertNode, new Object[]{"val1", "val2"});

        // When/Then
        assertEquals(insertValuesNode, insertValuesNode);
        assertEquals(insertValuesNode, insertValuesNode2);
        assertEquals(insertValuesNode.hashCode(), insertValuesNode2.hashCode());

        assertNotEquals(insertValuesNode, insertValuesNode3);
        assertEquals(insertValuesNode.hashCode(), insertValuesNode3.hashCode()); // Same length -> same hash

        assertNotEquals(insertValuesNode, insertValuesNodeDiffPrev);
        assertNotEquals(insertValuesNode.hashCode(), insertValuesNodeDiffPrev.hashCode());

        assertNotEquals(insertValuesNode, null);
        assertNotEquals(insertValuesNode, "string");
    }
}
