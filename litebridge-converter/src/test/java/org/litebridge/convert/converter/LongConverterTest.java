package org.litebridge.convert.converter;

import org.junit.jupiter.api.Test;

import java.sql.Types;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class LongConverterTest {

    private final LongConverter converter = new LongConverter();

    @Test
    void convert_null() {
        assertNull(converter.convert(null));
    }

    @Test
    void convert_Long() {
        // Given
        final Long input = 123L;

        // When
        final Long result = converter.convert(input);

        // Then
        assertSame(input, result);
    }

    @Test
    void convert_Number() {
        // Given
        final Integer input = 123;

        // When
        final Long result = converter.convert(input);

        // Then
        assertEquals(123L, result);
    }

    @Test
    void convert_String() {
        // Given
        final String input = "123";

        // When
        final Long result = converter.convert(input);

        // Then
        assertEquals(123L, result);
    }

    @Test
    void convert_BlankString() {
        assertNull(converter.convert(""));
        assertNull(converter.convert("  "));
    }

    @Test
    void type() {
        assertEquals(Long.class, converter.type());
    }

    @Test
    void sqlTypes() {
        assertArrayEquals(new int[]{Types.BIGINT}, converter.sqlTypes());
    }
}
