package org.litebridgedb.convert.converter;

import org.junit.jupiter.api.Test;

import java.sql.Types;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class StringConverterTest {

    private final StringConverter converter = new StringConverter();

    @Test
    void convert_null() {
        assertNull(converter.convert(null));
    }

    @Test
    void convert_String() {
        // Given
        final String input = "abc";

        // When
        final String result = converter.convert(input);

        // Then
        assertSame(input, result);
    }

    @Test
    void convert_ByteArray() {
        // Given
        final byte[] input = "abc".getBytes();

        // When
        final String result = converter.convert(input);

        // Then
        assertEquals("abc", result);
    }

    @Test
    void convert_CharArray() {
        // Given
        final char[] input = new char[]{'a', 'b', 'c'};

        // When
        final String result = converter.convert(input);

        // Then
        assertEquals("abc", result);
    }

    @Test
    void convert_ObjectArray() {
        // Given
        final Integer[] input = new Integer[]{1, 2, 3};

        // When
        final String result = converter.convert(input);

        // Then
        assertEquals(input.toString(), result);
    }

    @Test
    void convert_OtherObject() {
        // Given
        final Integer input = 123;

        // When
        final String result = converter.convert(input);

        // Then
        assertEquals("123", result);
    }

    @Test
    void type() {
        assertEquals(String.class, converter.type());
    }

    @Test
    void sqlTypes() {
        assertArrayEquals(new int[]{Types.CHAR, Types.VARCHAR, Types.LONGVARCHAR}, converter.sqlTypes());
    }
}
