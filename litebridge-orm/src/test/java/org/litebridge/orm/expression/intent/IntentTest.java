package org.litebridge.orm.expression.intent;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.Resolvable;
import org.litebridge.orm.expression.select.SelectColumnSpec;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class IntentTest {

    @Test
    void testConvertIntent() {
        final ExpressionSpec e1 = new SelectColumnSpec(mock(Column.class));
        final ExpressionSpec[] targets = new ExpressionSpec[]{e1};
        final ConvertIntent<Integer> intent = new ConvertIntent<>(targets, Integer.class);

        assertEquals(Integer.class, intent.returnType());
        assertArrayEquals(targets, intent.target());

        final ExpressionSpecArray array = intent.toExpression();
        assertArrayEquals(targets, array.expressions());
    }

    @Test
    void testConvertSpec() {
        final ExpressionSpec target = new SelectColumnSpec(mock(Column.class));
        final ConvertSpec<Integer> spec = new ConvertSpec<>(target, Integer.class);

        assertEquals(target, spec.target());
        assertEquals(Integer.class, spec.returnType());
        assertEquals("", spec.column());
        assertEquals(target.getClass(), spec.type());

        final ExpressionSpec newTarget = new SelectColumnSpec(mock(Column.class));
        final ConvertSpec<Integer> newSpec = spec.replaceTarget(newTarget);
        assertEquals(newTarget, newSpec.target());
        assertEquals(Integer.class, newSpec.returnType());
    }

    @Test
    void testExpressionSpecArray() {
        final ExpressionSpec e1 = new SelectColumnSpec(mock(Column.class));
        final ExpressionSpecArray array = new ExpressionSpecArray(new ExpressionSpec[]{e1});
        assertFalse(array.containsResolvable());

        final Resolvable e2 = mock(Resolvable.class);
        // We need an object that implements both ExpressionSpec and Resolvable
        // But ExpressionSpec is sealed.
        // Let's use ProtoColumnExpressionSpec which implements Resolvable and ExpressionSpec
        final ExpressionSpec resolvable = mock(org.litebridge.orm.expression.ProtoColumnExpressionSpec.class);
        final ExpressionSpecArray array2 = new ExpressionSpecArray(new ExpressionSpec[]{resolvable});
        assertTrue(array2.containsResolvable());
    }
}
