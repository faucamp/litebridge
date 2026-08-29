package org.litebridge.orm.expression.intent;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;

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
}
