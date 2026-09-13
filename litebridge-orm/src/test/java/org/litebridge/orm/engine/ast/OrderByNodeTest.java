package org.litebridge.orm.engine.ast;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderByNodeTest {

    @Test
    void recordComponentsAndGetters() {
        // Given
        final QueryNode previous = new DeleteNode(null, "p1", null);
        final String column = "created_at";
        final ExpressionSpec expr = new SelectColumnSpec(new Column(new Table("t"), "col1"));
        final boolean ascending = true;

        // When
        final OrderByNode node = new OrderByNode(previous, column, expr, ascending);

        // Then
        assertSame(previous, node.previous());
        assertEquals(column, node.column());
        assertSame(expr, node.expression());
        assertTrue(node.ascending());
    }

    @Test
    void equals_hashCode() {
        // Given
        final QueryNode prev1 = new DeleteNode(null, "p1", null);
        final QueryNode prev2 = new DeleteNode(null, "p2", null);
        final ExpressionSpec expr1 = new SelectColumnSpec(new Column(new Table("t"), "col1"));
        final ExpressionSpec expr2 = new SelectColumnSpec(new Column(new Table("t"), "col2"));

        final OrderByNode node1 = new OrderByNode(prev1, "col1", expr1, true);
        final OrderByNode node2 = new OrderByNode(prev1, "col1", expr1, true);

        final OrderByNode diffPrev = new OrderByNode(prev2, "col1", expr1, true);
        final OrderByNode diffCol = new OrderByNode(prev1, "col2", expr1, true);
        final OrderByNode diffExpr = new OrderByNode(prev1, "col1", expr2, true);
        final OrderByNode diffAsc = new OrderByNode(prev1, "col1", expr1, false);

        // When / Then
        assertEquals(node1, node1);
        assertEquals(node1, node2);
        assertEquals(node1.hashCode(), node2.hashCode());

        assertNotEquals(node1, diffPrev);
        assertNotEquals(node1, diffCol);
        assertNotEquals(node1, diffExpr);
        assertNotEquals(node1, diffAsc);
        assertNotEquals(node1, null);
        assertNotEquals(node1, "other");
    }
}
