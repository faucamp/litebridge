package org.litebridge.convert.converter;

import org.junit.jupiter.api.Test;

import java.sql.Types;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class FloatConverterTest {

    private final FloatConverter converter = new FloatConverter();

    @Test
    void convert_null() {
        assertNull(converter.convert(null));
    }

    @Test
    void convert_Float() {
        // Given
        final Float input = 123.45F;

        // When
        final Float result = converter.convert(input);

        // Then
        assertSame(input, result);
    }

    @Test
    void convert_Number() {
        // Given
        final Integer input = 123;

        // When
        final Float result = converter.convert(input);

        // Then
        assertEquals(123.0F, result);
    }

    @Test
    void convert_String() {
        // Given
        final String input = "123.45";

        // When
        final Float result = converter.convert(input);

        // Then
        assertEquals(123.45F, result);
    }

    @Test
    void convert_BlankString() {
        assertNull(converter.convert(""));
        assertNull(converter.convert("  "));
    }

    @Test
    void type() {
        assertEquals(Float.class, converter.type());
    }

    @Test
    void primitiveType() {
        assertEquals(float.class, converter.primitiveType());
    }

    @Test
    void sqlTypes() {
        assertArrayEquals(new int[]{Types.REAL}, converter.sqlTypes());
    }
}
