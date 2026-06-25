package org.litebridgedb.orm.expression.intent;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.orm.expression.ExpressionSpec;
import org.litebridgedb.orm.expression.select.SelectColumnSpec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class IntentExpressionTest {

    private ExpressionSpec mockTarget() {
        return new SelectColumnSpec(mock(Column.class));
    }

    @Test
    void testConvertSpec() {
        // Given
        final ExpressionSpec target = mockTarget();
        final Class<Integer> returnType = Integer.class;

        // When
        final ConvertSpec<Integer> spec = new ConvertSpec<>(target, returnType);

        // Then
        assertEquals(target, spec.target());
        assertEquals(returnType, spec.returnType());
        assertEquals("", spec.column());
        assertEquals(target.getClass(), spec.type());
    }

    @Test
    void testConvertIntent() {
        // Given
        final ExpressionSpec target = mockTarget();
        final Class<Integer> returnType = Integer.class;

        // When
        final ConvertIntent<Integer> intent = new ConvertIntent<>(new ExpressionSpec[]{target}, returnType);

        // Then
        assertEquals(1, intent.target().length);
        assertEquals(target, intent.target()[0]);
        assertEquals(returnType, intent.returnType());

        // When
        final ExpressionSpecArray spec = intent.toExpression();

        // Then
        assertEquals(1, spec.expressions().length);
        assertEquals(target, spec.expressions()[0]);
    }
}
