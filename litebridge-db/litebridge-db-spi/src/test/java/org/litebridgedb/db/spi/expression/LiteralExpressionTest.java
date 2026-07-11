package org.litebridgedb.db.spi.expression;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.Operation;

import org.litebridgedb.db.spi.query.Select;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.Mockito.mock;

class LiteralExpressionTest {

    @Test
    void value() {
        assertEquals("test", new LiteralExpression("test").value());
        assertNull(new LiteralExpression(null).value());
    }

    @Test
    void toSql() {
        final Select operation = mock(Select.class);
        final LiteralExpression stringLiteral = new LiteralExpression("test");
        assertEquals("test", stringLiteral.toSql(operation, ClauseType.SELECT, null));

        final LiteralExpression nullLiteral = new LiteralExpression(null);
        assertEquals("NULL", nullLiteral.toSql(operation, ClauseType.SELECT, null));

        final LiteralExpression listLiteral = new LiteralExpression(List.of(1, 2, 3));
        assertEquals("1, 2, 3", listLiteral.toSql(operation, ClauseType.SELECT, null));
    }

    @Test
    void toBindValueSql() {
        final Select operation = mock(Select.class);
        final LiteralExpression stringLiteral = new LiteralExpression("test");
        assertEquals("?", stringLiteral.toBindValueSql(operation));

        final LiteralExpression nullLiteral = new LiteralExpression(null);
        assertEquals("?", nullLiteral.toBindValueSql(operation));

        final LiteralExpression listLiteral = new LiteralExpression(List.of(1, 2, 3));
        assertEquals("?, ?, ?", listLiteral.toBindValueSql(operation));
    }

    @Test
    void equals_and_hashCode() {
        final LiteralExpression le1 = new LiteralExpression("test");
        final LiteralExpression le2 = new LiteralExpression("test");
        final LiteralExpression le3 = new LiteralExpression("other");
        final LiteralExpression leNull1 = new LiteralExpression(null);
        final LiteralExpression leNull2 = new LiteralExpression(null);

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
        final LiteralExpression le = new LiteralExpression("test");
        final String s = le.toString();
        assertTrue(s.contains("LiteralExpression"));
        assertTrue(s.contains("value=test"));
    }
}
