package org.litebridge.orm.engine.ast;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class InsertDtoValuesNodeTest {

    @Test
    void recordComponentsAndGetters() {
        // Given
        final QueryNode previous = new DeleteNode(null, "p1", null);
        final Object dto = "dto-instance";

        // When
        final InsertDtoValuesNode node = new InsertDtoValuesNode(previous, dto);

        // Then
        assertSame(previous, node.previous());
        assertEquals(dto, node.dto());
    }

    @Test
    void equals_hashCode() {
        // Given
        final QueryNode prev1 = new DeleteNode(null, "p1", null);
        final QueryNode prev2 = new DeleteNode(null, "p2", null);

        final InsertDtoValuesNode node1 = new InsertDtoValuesNode(prev1, "dto1");
        final InsertDtoValuesNode node2 = new InsertDtoValuesNode(prev1, "dto1");

        final InsertDtoValuesNode diffPrev = new InsertDtoValuesNode(prev2, "dto1");
        final InsertDtoValuesNode diffDto = new InsertDtoValuesNode(prev1, "dto2");

        // When / Then
        assertEquals(node1, node1);
        assertEquals(node1, node2);
        assertEquals(node1.hashCode(), node2.hashCode());

        assertNotEquals(node1, diffPrev);
        assertNotEquals(node1, diffDto);
        assertEquals(node1.hashCode(), diffDto.hashCode()); // hashCode only hashes previous!

        assertNotEquals(node1, null);
        assertNotEquals(node1, "other");
    }
}
