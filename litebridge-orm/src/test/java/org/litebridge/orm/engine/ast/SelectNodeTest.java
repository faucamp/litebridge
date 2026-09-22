package org.litebridge.orm.engine.ast;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SelectNodeTest {

    @Test
    void recordComponentsAndGetters() {
        // Given
        final String table = "USERS";
        final Class<?> dtoClass = String.class;
        final Class<?> contextDto = Integer.class;
        final String[] columns = new String[]{"id", "name"};
        final ExpressionSpec expr = new SelectColumnSpec(new Column(new Table("t"), "col1"));
        final ExpressionSpec[] expressions = new ExpressionSpec[]{expr};
        final Class<?>[] resultTypes = new Class<?>[]{String.class, String.class};

        // When
        final SelectNode node = new SelectNode(table, dtoClass, contextDto, null, "alias1", columns, expressions, resultTypes);

        // Then
        assertNull(node.previous());
        assertEquals(table, node.table());
        assertEquals(dtoClass, node.dtoClass());
        assertEquals(contextDto, node.contextDtoClass());
        assertArrayEquals(columns, node.columns());
        assertNull(node.fromQueryNode());
        assertEquals("alias1", node.alias());
        assertArrayEquals(expressions, node.expressions());
        assertArrayEquals(resultTypes, node.resultTypes());
    }

    @Test
    void isSelectAllBranches() {
        // Given
        final SelectNode selectAll = new SelectNode("USERS", null, null, null, null);
        final SelectNode selectColumns = new SelectNode("USERS", null, new String[]{"id"}, null, null);
        final ExpressionSpec expr = new SelectColumnSpec(new Column(new Table("t"), "col1"));
        final SelectNode selectExpr = new SelectNode("USERS", null, null, new ExpressionSpec[]{expr}, null);
        final SelectNode selectBoth = new SelectNode("USERS", null, new String[]{"id"}, new ExpressionSpec[]{expr}, null);

        // When / Then
        assertTrue(selectAll.isSelectAll());
        assertFalse(selectColumns.isSelectAll());
        assertFalse(selectExpr.isSelectAll());
        assertFalse(selectBoth.isSelectAll());
    }

    @Test
    void equals_hashCode() {
        // Given
        final ExpressionSpec expr1 = new SelectColumnSpec(new Column(new Table("t"), "col1"));
        final ExpressionSpec expr2 = new SelectColumnSpec(new Column(new Table("t"), "col2"));

        final SelectNode base = new SelectNode(String.class, Integer.class, null,
                new String[]{"c1"}, new ExpressionSpec[]{expr1}, new Class<?>[]{Long.class});
        final SelectNode same = new SelectNode(String.class, Integer.class, null,
                new String[]{"c1"}, new ExpressionSpec[]{expr1}, new Class<?>[]{Long.class});

        final SelectNode diffTable = new SelectNode(String.class, Integer.class, null,
                new String[]{"c1"}, new ExpressionSpec[]{expr1}, new Class<?>[]{Long.class});
        final SelectNode diffDto = new SelectNode(String.class, Double.class, null,
                new String[]{"c1"}, new ExpressionSpec[]{expr1}, new Class<?>[]{Long.class});
        final SelectNode diffContext = new SelectNode(String.class, Integer.class, null,
                new String[]{"c1"}, new ExpressionSpec[]{expr1}, new Class<?>[]{Long.class});
        final SelectNode diffCols = new SelectNode(String.class, Integer.class, null,
                new String[]{"c2"}, new ExpressionSpec[]{expr1}, new Class<?>[]{Long.class});
        final SelectNode diffExprs = new SelectNode(String.class, Integer.class, null,
                new String[]{"c1"}, new ExpressionSpec[]{expr2}, new Class<?>[]{Long.class});
        final SelectNode diffTypes = new SelectNode(String.class, Integer.class, null,
                new String[]{"c1"}, new ExpressionSpec[]{expr1}, new Class<?>[]{Short.class});

        // When / Then
        assertEquals(base, base);
        assertEquals(base, same);
        assertEquals(base.hashCode(), same.hashCode());

        assertNotEquals(base, diffDto);
        assertNotEquals(base, diffCols);
        assertNotEquals(base, diffExprs);
        assertNotEquals(base, diffTypes);

        assertNotEquals(base, null);
        assertNotEquals(base, "other");
    }
}
