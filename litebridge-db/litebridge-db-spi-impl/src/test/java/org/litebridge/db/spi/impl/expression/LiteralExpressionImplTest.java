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

        final LiteralExpression stringLiteralSelectWithAlias = new LiteralExpressionImpl("test", "my_alias", labelGenerator);
        assertEquals("? AS \"my_alias\"", stringLiteralSelectWithAlias.toSql(operation, ClauseType.SELECT, null));

        final LiteralExpression joinClauseLiteral = new LiteralExpressionImpl("test", "my_alias", labelGenerator);
        assertEquals("?", joinClauseLiteral.toSql(operation, ClauseType.JOIN, null));

        final LiteralExpression valuesClauseLiteral = new LiteralExpressionImpl("test", "my_alias", labelGenerator);
        assertEquals("?", valuesClauseLiteral.toSql(operation, ClauseType.VALUES, null));

        final LiteralExpression stringLiteral = new LiteralExpressionImpl("test", labelGenerator);
        assertEquals("'test'", stringLiteral.toSql(operation, ClauseType.ORDER_BY, null));

        final LiteralExpression quoteLiteral = new LiteralExpressionImpl("O'Reilly", labelGenerator);
        assertEquals("'O''Reilly'", quoteLiteral.toSql(operation, ClauseType.ORDER_BY, null));

        final LiteralExpression intLiteral = new LiteralExpressionImpl(42, labelGenerator);
        assertEquals("42", intLiteral.toSql(operation, ClauseType.ORDER_BY, null));

        final LiteralExpression nullLiteral = new LiteralExpressionImpl(null, labelGenerator);
        assertEquals("NULL", nullLiteral.toSql(operation, ClauseType.ORDER_BY, null));

        final LiteralExpression listLiteral = new LiteralExpressionImpl(List.of(1, 2, 3), labelGenerator);
        assertEquals("1, 2, 3", listLiteral.toSql(operation, ClauseType.ORDER_BY, null));
    }

    @Test
    void toBindValueSql() {
        final LiteralExpressionImpl singleLiteral = new LiteralExpressionImpl("val", "alias", labelGenerator);
        assertEquals("? AS \"alias\"", singleLiteral.toBindValueSql(ClauseType.SELECT));
        assertEquals("?", singleLiteral.toBindValueSql(ClauseType.WHERE));

        final LiteralExpressionImpl collectionLiteral = new LiteralExpressionImpl(List.of(1, 2, 3), "alias", labelGenerator);
        assertEquals("?, ?, ? AS \"alias\"", collectionLiteral.toBindValueSql(ClauseType.SELECT));
        assertEquals("?, ?, ?", collectionLiteral.toBindValueSql(ClauseType.WHERE));
    }

    @Test
    void equals_and_hashCode() {
        final LiteralExpression le1 = new LiteralExpressionImpl("test", "a1", labelGenerator);
        final LiteralExpression le2 = new LiteralExpressionImpl("test", "a1", labelGenerator);
        final LiteralExpression le3 = new LiteralExpressionImpl("other", "a1", labelGenerator);
        final LiteralExpression le4 = new LiteralExpressionImpl("test", "a2", labelGenerator);
        final LiteralExpression leNull1 = new LiteralExpressionImpl(null, labelGenerator);
        final LiteralExpression leNull2 = new LiteralExpressionImpl(null, labelGenerator);

        assertEquals(le1, le1);
        assertEquals(le1, le2);
        assertNotEquals(le1, le3);
        assertNotEquals(le1, le4);
        assertNotEquals(le1, null);
        assertNotEquals(le1, "test");
        assertEquals(leNull1, leNull2);
        assertNotEquals(le1, leNull1);

        assertEquals(le1.hashCode(), le2.hashCode());
        assertEquals(leNull1.hashCode(), leNull2.hashCode());
    }

    @Test
    void testToString() {
        final LiteralExpression le = new LiteralExpressionImpl("test", "my_alias", labelGenerator);
        final String s = le.toString();
        assertTrue(s.contains("LiteralExpression"));
        assertTrue(s.contains("value=test"));
        assertTrue(s.contains("alias='my_alias'"));
    }
}
