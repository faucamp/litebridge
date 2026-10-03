package org.litebridge.orm.engine.ast;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class InsertNodeTest {

    @Test
    void constructorWithColumnsAndGetters() {
        // Given
        final String table = "USERS";
        final Class<?> dtoClass = String.class;
        final String[] columns = new String[]{"COL1", "COL2"};

        // When
        final InsertNode node = new InsertNode(table, dtoClass, columns);

        // Then
        assertNull(node.previous());
        assertEquals(table, node.table());
        assertEquals(dtoClass, node.dtoClass());
        assertNull(node.contextDtoClass());
        assertArrayEquals(columns, node.columns());
        assertNull(node.expressionSpecs());
    }

    @Test
    void constructorWithExpressionSpecsAndGetters() {
        // Given
        final String table = "USERS";
        final Class<?> dtoClass = String.class;
        final ExpressionSpec spec1 = new SelectColumnSpec(new Column(new Table("t"), "col1"));
        final ExpressionSpec[] specs = new ExpressionSpec[]{spec1};

        // When
        final InsertNode node = new InsertNode(table, dtoClass, specs);

        // Then
        assertNull(node.previous());
        assertEquals(table, node.table());
        assertEquals(dtoClass, node.dtoClass());
        assertNull(node.contextDtoClass());
        assertNull(node.columns());
        assertArrayEquals(specs, node.expressionSpecs());
    }

    @Test
    void fullConstructorAndGetters() {
        // Given
        final String table = "USERS";
        final Class<?> dtoClass = String.class;
        final Class<?> contextDto = Integer.class;
        final String[] columns = new String[]{"COL1"};
        final ExpressionSpec spec1 = new SelectColumnSpec(new Column(new Table("t"), "col1"));
        final ExpressionSpec[] specs = new ExpressionSpec[]{spec1};

        // When
        final InsertNode node = new InsertNode(table, dtoClass, contextDto, columns, specs);

        // Then
        assertNull(node.previous());
        assertEquals(table, node.table());
        assertEquals(dtoClass, node.dtoClass());
        assertEquals(contextDto, node.contextDtoClass());
        assertArrayEquals(columns, node.columns());
        assertArrayEquals(specs, node.expressionSpecs());
    }

    @Test
    void equals_hashcode() {
        // Given
        final InsertNode sqlInsertNode = new InsertNode("TEST_TABLE", null, new String[]{"COL1", "COL2"});
        final InsertNode sqlInsertNode2 = new InsertNode("TEST_TABLE", null, new String[]{"COL1", "COL2"});
        final InsertNode sqlInsertNode3 = new InsertNode("TEST_TABLE", null, new String[]{"COL2", "COL3"});
        final InsertNode sqlInsertNodeDiffTable = new InsertNode("OTHER_TABLE", null, new String[]{"COL1", "COL2"});
        final InsertNode sqlInsertNodeDiffDto = new InsertNode("TEST_TABLE", String.class, new String[]{"COL1", "COL2"});

        final ExpressionSpec spec1 = new SelectColumnSpec(new Column(new Table("t"), "col1"));
        final ExpressionSpec spec2 = new SelectColumnSpec(new Column(new Table("t"), "col2"));
        final InsertNode sqlInsertNodeExpr = new InsertNode("TEST_TABLE", null, new ExpressionSpec[]{spec1});
        final InsertNode sqlInsertNodeExpr2 = new InsertNode("TEST_TABLE", null, new ExpressionSpec[]{spec2});

        // When/Then
        assertEquals(sqlInsertNode, sqlInsertNode);
        assertEquals(sqlInsertNode, sqlInsertNode2);
        assertEquals(sqlInsertNode.hashCode(), sqlInsertNode2.hashCode());

        assertNotEquals(sqlInsertNode, sqlInsertNode3);
        assertNotEquals(sqlInsertNode.hashCode(), sqlInsertNode3.hashCode());

        assertNotEquals(sqlInsertNode, sqlInsertNodeDiffTable);
        assertNotEquals(sqlInsertNode, sqlInsertNodeDiffDto);
        assertNotEquals(sqlInsertNode, sqlInsertNodeExpr);
        assertNotEquals(sqlInsertNodeExpr, sqlInsertNodeExpr2);
        assertNotEquals(sqlInsertNodeExpr.hashCode(), sqlInsertNodeExpr2.hashCode());
        assertNotEquals(sqlInsertNode, null);
        assertNotEquals(sqlInsertNode, "some-string");
    }
}
