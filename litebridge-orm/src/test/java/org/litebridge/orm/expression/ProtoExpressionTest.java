package org.litebridge.orm.expression;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.orm.expression.select.SelectColumnSpec;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class ProtoExpressionTest {

    @Test
    void testProtoColumnExpressionSpec() {
        final ProtoColumnExpressionSpec spec = new ProtoColumnExpressionSpec(SelectColumnSpec.class, "COL", "alias");
        assertEquals(SelectColumnSpec.class, spec.type());
        assertEquals("COL", spec.column());
        assertEquals("alias", spec.alias());
        assertNull(spec.args());

        final ProtoColumnExpressionSpec spec2 = new ProtoColumnExpressionSpec(SelectColumnSpec.class, "COL");
        assertNull(spec2.alias());

        assertThrows(IllegalArgumentException.class, () -> new ProtoColumnExpressionSpec(ExpressionSpec.class, "COL"));
    }

    @Test
    void testProtoNestableTOExpr() {
        final ExpressionSpec target = new SelectColumnSpec(mock(Column.class));
        final ProtoNestableTOExpr<String> spec = new ProtoNestableTOExpr<>(String.class, SelectColumnSpec.class, target, "alias");

        assertEquals(String.class, spec.returnType());
        assertEquals(SelectColumnSpec.class, spec.type());
        assertEquals(target, spec.target());
        assertEquals("alias", spec.alias());
        assertNull(spec.args());
        assertNull(spec.column());

        final ProtoNestableTOExpr<String> spec2 = new ProtoNestableTOExpr<>(String.class, SelectColumnSpec.class, "COL", "alias");
        assertEquals("COL", spec2.column());
        assertInstanceOf(ProtoColumnExpressionSpec.class, spec2.target());

        final Object[] args = new Object[]{"arg1"};
        final ProtoNestableTOExpr<String> spec3 = new ProtoNestableTOExpr<>(String.class, SelectColumnSpec.class, "COL", "alias", args);
        assertArrayEquals(args, spec3.args());
    }
}
