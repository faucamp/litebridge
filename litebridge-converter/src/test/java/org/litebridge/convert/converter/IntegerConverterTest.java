package org.litebridge.convert.converter;

import org.junit.jupiter.api.Test;

import java.sql.Types;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class IntegerConverterTest {

    private final IntegerConverter converter = new IntegerConverter();

    @Test
    void convert_null() {
        assertNull(converter.convert(null));
    }

    @Test
    void convert_Integer() {
        // Given
        final Integer input = 123;

        // When
        final Integer result = converter.convert(input);

        // Then
        assertSame(input, result);
    }

    @Test
    void convert_Number() {
        // Given
        final Long input = 123L;

        // When
        final Integer result = converter.convert(input);

        // Then
        assertEquals(123, result);
    }

    @Test
    void convert_String() {
        // Given
        final String input = "123";

        // When
        final Integer result = converter.convert(input);

        // Then
        assertEquals(123, result);
    }

    @Test
    void convert_BlankString() {
        assertNull(converter.convert(""));
        assertNull(converter.convert("  "));
    }

    @Test
    void type() {
        assertEquals(Integer.class, converter.type());
    }

    @Test
    void sqlTypes() {
        assertArrayEquals(new int[]{Types.INTEGER}, converter.sqlTypes());
    }
}
