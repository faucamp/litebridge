package org.litebridgedb.orm.expression;

import org.junit.jupiter.api.Test;
import org.litebridgedb.orm.expression.function.scalar.UpperSpec;
import org.litebridgedb.orm.expression.intent.ConvertIntent;
import org.litebridgedb.orm.expression.intent.ConvertSpec;
import org.litebridgedb.orm.expression.intent.ExpressionSpecArray;
import org.litebridgedb.orm.expression.select.SelectColumnSpec;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BaseExpressionTest {

    @Test
    void testProtoColumnExpressionSpec() {
        // When
        final ProtoColumnExpressionSpec spec = new ProtoColumnExpressionSpec(SelectColumnSpec.class, "my_column", "my_alias");

        // Then
        assertEquals(SelectColumnSpec.class, spec.type());
        assertEquals("my_column", spec.column());
        assertEquals("my_alias", spec.alias());
        assertNull(spec.args());

        // When
        final ProtoColumnExpressionSpec spec2 = new ProtoColumnExpressionSpec(SelectColumnSpec.class, "my_column");

        // Then
        assertEquals("my_column", spec2.column());
        assertNull(spec2.alias());

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> new ProtoColumnExpressionSpec(ExpressionSpec.class, "col"));
    }

    @Test
    void testProtoNestableBasicExprSpec() {
        // Given
        final ProtoColumnExpressionSpec target = new ProtoColumnExpressionSpec(SelectColumnSpec.class, "col");

        // When
        final ProtoNestableBasicExprSpec spec = new ProtoNestableBasicExprSpec(UpperSpec.class, target, "alias");

        // Then
        assertEquals(UpperSpec.class, spec.type());
        assertEquals(target, spec.target());
        assertEquals("alias", spec.alias());
        assertNull(spec.args());
        assertEquals("col", spec.column());

        // When
        final ProtoNestableBasicExprSpec spec2 = new ProtoNestableBasicExprSpec(UpperSpec.class, "col2", "alias2");

        // Then
        assertEquals("col2", spec2.column());

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> new ProtoNestableBasicExprSpec(ExpressionSpec.class, target, "alias"));
    }

    @Test
    void testProtoNestableTOExpr() {
        // Given
        final ProtoColumnExpressionSpec target = new ProtoColumnExpressionSpec(SelectColumnSpec.class, "col");
        final Object[] args = new Object[]{"arg1"};

        // When
        final ProtoNestableTOExpr<String> spec = new ProtoNestableTOExpr<>(String.class, UpperSpec.class, target, "alias", args);

        // Then
        assertEquals(String.class, spec.returnType());
        assertEquals(UpperSpec.class, spec.type());
        assertEquals(target, spec.target());
        assertEquals("alias", spec.alias());
        assertArrayEquals(args, spec.args());
        assertEquals("col", spec.column());

        // Further scenarios
        final ProtoNestableTOExpr<String> spec2 = new ProtoNestableTOExpr<>(String.class, UpperSpec.class, target, "alias");
        assertNull(spec2.args());

        final ProtoNestableTOExpr<String> spec3 = new ProtoNestableTOExpr<>(String.class, UpperSpec.class, "col3", "alias3");
        assertEquals("col3", spec3.column());

        final ProtoNestableTOExpr<String> spec4 = new ProtoNestableTOExpr<>(String.class, UpperSpec.class, "col4", "alias4", args);
        assertEquals("col4", spec4.column());
        assertArrayEquals(args, spec4.args());
    }

    @Test
    void testConvertIntent() {
        // Given
        final ExpressionSpec target = new SelectColumnSpec(null);

        // When
        final ConvertIntent<Integer> intent = new ConvertIntent<>(new ExpressionSpec[]{target}, Integer.class);

        // Then
        final ExpressionSpec[] expectedTarget = new ExpressionSpec[]{target};
        assertArrayEquals(expectedTarget, intent.target());
        assertEquals(Integer.class, intent.returnType());

        // When
        final ExpressionSpecArray spec = intent.toExpression();

        // Then
        assertArrayEquals(expectedTarget, spec.expressions());
    }
}
