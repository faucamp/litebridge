package org.litebridge.orm.api.select.ast;

import org.junit.jupiter.api.Test;
import org.litebridge.orm.engine.ast.InsertNode;
import org.litebridge.orm.engine.ast.InsertValuesNode;

import static org.junit.jupiter.api.Assertions.*;

class InsertValuesNodeTest {

    @Test
    void equals_hashCode() {
        // Given
        final InsertNode insertNode = new InsertNode("TEST_TABLE", null, new String[]{"COL1, COL2"});
        final InsertValuesNode insertValuesNode = new InsertValuesNode(insertNode, new Object[] {"val1", "val2"});
        final InsertValuesNode insertValuesNode2 = new InsertValuesNode(insertNode, new Object[] {"val1", "val2"});
        final InsertValuesNode insertValuesNode3 = new InsertValuesNode(insertNode, new Object[] {"val2", "val3"});

        // When/Then
        assertEquals(insertValuesNode, insertValuesNode2);
        assertEquals(insertValuesNode.hashCode(), insertValuesNode2.hashCode());
        assertNotEquals(insertValuesNode, insertValuesNode3);
        assertEquals(insertValuesNode.hashCode(), insertValuesNode3.hashCode());
    }
}