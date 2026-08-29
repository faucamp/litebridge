package org.litebridge.orm.api.select.ast;

import org.junit.jupiter.api.Test;
import org.litebridge.orm.engine.ast.InsertNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class InsertNodeTest {

    @Test
    public void equals_hashcode() {
        // Given
        final InsertNode sqlInsertNode = new InsertNode("TEST_TABLE", null, new String[]{"COL1, COL2"});
        final InsertNode sqlInsertNode2 = new InsertNode("TEST_TABLE", null, new String[]{"COL1, COL2"});
        final InsertNode sqlInsertNode3 = new InsertNode("TEST_TABLE", null, new String[]{"COL2, COL3"});

        // When/Then
        assertEquals(sqlInsertNode, sqlInsertNode2);
        assertEquals(sqlInsertNode.hashCode(), sqlInsertNode2.hashCode());
        assertNotEquals(sqlInsertNode, sqlInsertNode3);
        assertNotEquals(sqlInsertNode.hashCode(), sqlInsertNode3.hashCode());
    }

}