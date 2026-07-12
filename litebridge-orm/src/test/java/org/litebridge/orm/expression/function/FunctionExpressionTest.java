package org.litebridge.orm.expression.function;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.orm.expression.ColumnExpressionSpec;
import org.litebridge.orm.expression.function.aggregate.AvgSpec;
import org.litebridge.orm.expression.function.aggregate.CountSpec;
import org.litebridge.orm.expression.function.aggregate.MaxSpec;
import org.litebridge.orm.expression.function.aggregate.MinSpec;
import org.litebridge.orm.expression.function.date.CurrentTimestampSpec;
import org.litebridge.orm.expression.function.scalar.AbsSpec;
import org.litebridge.orm.expression.function.scalar.LowerSpec;
import org.litebridge.orm.expression.function.scalar.SubstringSpec;
import org.litebridge.orm.expression.function.scalar.UpperSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class FunctionExpressionTest {

    private ColumnExpressionSpec mockTarget() {
        return new SelectColumnSpec(mock(Column.class));
    }

    @Test
    void testAvgSpec() {
        // Given
        final ColumnExpressionSpec target = mockTarget();

        // When
        final AvgSpec<Double> spec = new AvgSpec<>(target, Double.class);

        // Then
        assertEquals(target, spec.target());
        assertEquals(Double.class, spec.returnType());
    }

    @Test
    void testCountSpec() {
        // When
        final CountSpec spec = new CountSpec();

        // Then
        assertEquals(Long.class, spec.returnType());
    }

    @Test
    void testMaxSpec() {
        // Given
        final ColumnExpressionSpec target = mockTarget();

        // When
        final MaxSpec<Integer> spec = new MaxSpec<>(target, Integer.class);

        // Then
        assertEquals(target, spec.target());
        assertEquals(Integer.class, spec.returnType());
    }

    @Test
    void testMinSpec() {
        // Given
        final ColumnExpressionSpec target = mockTarget();

        // When
        final MinSpec<Integer> spec = new MinSpec<>(target, Integer.class);

        // Then
        assertEquals(target, spec.target());
        assertEquals(Integer.class, spec.returnType());
    }

    @Test
    void testCurrentTimestampSpec() {
        // When
        final CurrentTimestampSpec spec = new CurrentTimestampSpec();

        // Then
        assertEquals(ZonedDateTime.class, spec.returnType());
    }

    @Test
    void testAbsSpec() {
        // Given
        final ColumnExpressionSpec target = mockTarget();

        // When
        final AbsSpec spec = new AbsSpec(target);

        // Then
        assertEquals(target, spec.target());
        assertEquals(Number.class, spec.returnType());
    }

    @Test
    void testLowerSpec() {
        // Given
        final ColumnExpressionSpec target = mockTarget();

        // When
        final LowerSpec spec = new LowerSpec(target);

        // Then
        assertEquals(target, spec.target());
        assertEquals(String.class, spec.returnType());
    }

    @Test
    void testUpperSpec() {
        // Given
        final ColumnExpressionSpec target = mockTarget();

        // When
        final UpperSpec spec = new UpperSpec(target);

        // Then
        assertEquals(target, spec.target());
        assertEquals(String.class, spec.returnType());
    }

    @Test
    void testSubstringSpec() {
        // Given
        final ColumnExpressionSpec target = mockTarget();

        // When
        final SubstringSpec spec = new SubstringSpec(target, 1, 10);

        // Then
        assertEquals(target, spec.target());
        assertEquals(1, spec.start());
        assertEquals(10, spec.length());
        assertEquals(String.class, spec.returnType());
    }
}
