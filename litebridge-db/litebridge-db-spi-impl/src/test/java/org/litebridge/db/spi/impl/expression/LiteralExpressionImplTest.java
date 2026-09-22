package org.litebridge.db.spi.impl.expression;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.expression.LiteralExpression;
import org.litebridge.db.spi.impl.sql.LabelGenerator;
import org.litebridge.db.spi.query.Select;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.Mockito.mock;

class LiteralExpressionImplTest {

    private static final LabelGenerator labelGenerator = new LabelGenerator();

    @Test
    void value() {
        Assertions.assertEquals("test", new LiteralExpressionImpl("test", labelGenerator).value());
        assertNull(new LiteralExpressionImpl(null, labelGenerator).value());
    }

    @Test
    void toSql() {
        final Select operation = mock(Select.class);
        final LiteralExpression stringLiteralSelect = new LiteralExpressionImpl("test", labelGenerator);
        assertEquals("?", stringLiteralSelect.toSql(operation, ClauseType.SELECT, null));

        final LiteralExpression stringLiteral = new LiteralExpressionImpl("test", labelGenerator);
        assertEquals("'test'", stringLiteral.toSql(operation, ClauseType.ORDER_BY, null));

        final LiteralExpression nullLiteral = new LiteralExpressionImpl(null, labelGenerator);
        assertEquals("NULL", nullLiteral.toSql(operation, ClauseType.ORDER_BY, null));

        final LiteralExpression listLiteral = new LiteralExpressionImpl(List.of(1, 2, 3), labelGenerator);
        assertEquals("1, 2, 3", listLiteral.toSql(operation, ClauseType.ORDER_BY, null));
    }

    @Test
    void equals_and_hashCode() {
        final LiteralExpression le1 = new LiteralExpressionImpl("test", labelGenerator);
        final LiteralExpression le2 = new LiteralExpressionImpl("test", labelGenerator);
        final LiteralExpression le3 = new LiteralExpressionImpl("other", labelGenerator);
        final LiteralExpression leNull1 = new LiteralExpressionImpl(null, labelGenerator);
        final LiteralExpression leNull2 = new LiteralExpressionImpl(null, labelGenerator);

        assertEquals(le1, le1);
        assertEquals(le1, le2);
        assertNotEquals(le1, le3);
        assertNotEquals(le1, null);
        assertNotEquals(le1, "test");
        assertEquals(leNull1, leNull2);
        assertNotEquals(le1, leNull1);

        assertEquals(le1.hashCode(), le2.hashCode());
        assertEquals(leNull1.hashCode(), leNull2.hashCode());
    }

    @Test
    void testToString() {
        final LiteralExpression le = new LiteralExpressionImpl("test", labelGenerator);
        final String s = le.toString();
        assertTrue(s.contains("LiteralExpression"));
        assertTrue(s.contains("value=test"));
    }
}
