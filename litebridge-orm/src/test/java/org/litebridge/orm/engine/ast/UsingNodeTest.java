package org.litebridge.orm.engine.ast;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class UsingNodeTest {

    @Test
    void recordComponentsAndGetters() {
        // Given
        final MergeNode previous = new MergeNode("TARGET", null);
        final String table = "SOURCE";
        final Class<?> dtoClass = String.class;
        final QueryNode on = new ConditionWithIdNode(null, LogicOperator.AND, Operator.EQ, 1);

        // When
        final UsingNode node = new UsingNode(previous, table, dtoClass, on);

        // Then
        assertSame(previous, node.previous());
        assertEquals(table, node.table());
        assertEquals(dtoClass, node.dtoClass());
        assertSame(on, node.on());
    }

    @Test
    void equals_hashCode() {
        // Given
        final MergeNode prev1 = new MergeNode("T1", null);
        final MergeNode prev2 = new MergeNode("T2", null);
        final QueryNode on1 = new ConditionWithIdNode(null, LogicOperator.AND, Operator.EQ, 1);
        final QueryNode on2 = new ConditionWithIdNode(null, LogicOperator.OR, Operator.EQ, 1);

        final UsingNode node1 = new UsingNode(prev1, "SRC", String.class, on1);
        final UsingNode node2 = new UsingNode(prev1, "SRC", String.class, on1);

        final UsingNode diffPrev = new UsingNode(prev2, "SRC", String.class, on1);
        final UsingNode diffTable = new UsingNode(prev1, "OTHER", String.class, on1);
        final UsingNode diffDto = new UsingNode(prev1, "SRC", Integer.class, on1);
        final UsingNode diffOn = new UsingNode(prev1, "SRC", String.class, on2);

        // When / Then
        assertEquals(node1, node1);
        assertEquals(node1, node2);
        assertEquals(node1.hashCode(), node2.hashCode());

        assertNotEquals(node1, diffPrev);
        assertNotEquals(node1, diffTable);
        assertNotEquals(node1, diffDto);
        assertNotEquals(node1, diffOn);

        assertNotEquals(node1, null);
        assertNotEquals(node1, "other");
    }
}
