package org.litebridge.orm.engine.ast;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class GroupByNodeTest {

    @Test
    void recordComponentsAndGetters() {
        // Given
        final QueryNode previous = new DeleteNode(null, "p1", null);
        final String[] columns = new String[]{"category", "year"};
        final ExpressionSpec expr1 = new SelectColumnSpec(new Column(new Table("t"), "col1"));
        final ExpressionSpec[] expressions = new ExpressionSpec[]{expr1};

        // When
        final GroupByNode node = new GroupByNode(previous, columns, expressions);

        // Then
        assertSame(previous, node.previous());
        assertArrayEquals(columns, node.columns());
        assertArrayEquals(expressions, node.expressions());
    }

    @Test
    void equals_hashCode() {
        // Given
        final QueryNode prev1 = new DeleteNode(null, "p1", null);
        final QueryNode prev2 = new DeleteNode(null, "p2", null);
        final ExpressionSpec expr1 = new SelectColumnSpec(new Column(new Table("t"), "col1"));
        final ExpressionSpec expr2 = new SelectColumnSpec(new Column(new Table("t"), "col2"));

        final String[] cols1 = new String[]{"c1"};
        final ExpressionSpec[] exprs1 = new ExpressionSpec[]{expr1};
        final String[] cols2 = new String[]{"c2"};
        final ExpressionSpec[] exprs2 = new ExpressionSpec[]{expr2};

        final GroupByNode node1 = new GroupByNode(prev1, cols1, exprs1);
        final GroupByNode node2 = new GroupByNode(prev1, cols1, exprs1);

        final GroupByNode diffPrev = new GroupByNode(prev2, cols1, exprs1);
        final GroupByNode diffCols = new GroupByNode(prev1, cols2, exprs1);
        final GroupByNode diffExprs = new GroupByNode(prev1, cols1, exprs2);

        // When / Then
        assertEquals(node1, node1);
        assertEquals(node1, node2);
        assertEquals(node1.hashCode(), node2.hashCode());

        assertNotEquals(node1, diffPrev);
        assertNotEquals(node1, diffCols);
        assertNotEquals(node1, diffExprs);
        assertNotEquals(node1, null);
        assertNotEquals(node1, "other");
    }
}
