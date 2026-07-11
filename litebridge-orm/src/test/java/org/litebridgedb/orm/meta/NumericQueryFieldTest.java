package org.litebridgedb.orm.meta;

import org.junit.jupiter.api.Test;
import org.litebridgedb.orm.expression.TypeOverrideExpressionSpec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class NumericQueryFieldTest {

    @Test
    void testConstructor() {
        // Given
        final NumericQueryField field = new NumericQueryField(TestDto.class, "age");

        // Then
        assertEquals(TestDto.class, field.dtoClass());
        assertEquals("age", field.field());
    }

    @Test
    void testAvg() {
        // Given
        final NumericQueryField field = new NumericQueryField(TestDto.class, "age");

        // When
        final TypeOverrideExpressionSpec<Number> avg = field.avg();

        // Then
        assertNotNull(avg);
        assertEquals(Number.class, avg.returnType());
    }

    @Test
    void testMax() {
        // Given
        final NumericQueryField field = new NumericQueryField(TestDto.class, "age");

        // When
        final TypeOverrideExpressionSpec<Number> max = field.max();

        // Then
        assertNotNull(max);
        assertEquals(Number.class, max.returnType());
    }

    @Test
    void testMin() {
        // Given
        final NumericQueryField field = new NumericQueryField(TestDto.class, "age");

        // When
        final TypeOverrideExpressionSpec<Number> min = field.min();

        // Then
        assertNotNull(min);
        assertEquals(Number.class, min.returnType());
    }
}
