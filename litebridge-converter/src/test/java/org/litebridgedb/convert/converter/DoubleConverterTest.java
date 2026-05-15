package org.litebridgedb.convert.converter;

import org.junit.jupiter.api.Test;

import java.sql.Types;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class DoubleConverterTest {

    private final DoubleConverter converter = new DoubleConverter();

    @Test
    void convert_null() {
        assertNull(converter.convert(null));
    }

    @Test
    void convert_Double() {
        // Given
        final Double input = 123.45;

        // When
        final Double result = converter.convert(input);

        // Then
        assertSame(input, result);
    }

    @Test
    void convert_Number() {
        // Given
        final Integer input = 123;

        // When
        final Double result = converter.convert(input);

        // Then
        assertEquals(123.0, result);
    }

    @Test
    void convert_String() {
        // Given
        final String input = "123.45";

        // When
        final Double result = converter.convert(input);

        // Then
        assertEquals(123.45, result);
    }

    @Test
    void convert_BlankString() {
        assertNull(converter.convert(""));
        assertNull(converter.convert("  "));
    }

    @Test
    void type() {
        assertEquals(Double.class, converter.type());
    }

    @Test
    void primitiveType() {
        assertEquals(double.class, converter.primitiveType());
    }

    @Test
    void sqlTypes() {
        assertArrayEquals(new int[]{Types.FLOAT, Types.DOUBLE}, converter.sqlTypes());
    }
}
