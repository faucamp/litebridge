package org.litebridge.convert.converter;

import org.junit.jupiter.api.Test;

import java.sql.Types;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class ByteConverterTest {

    private final ByteConverter converter = new ByteConverter();

    @Test
    void convert_null() {
        assertNull(converter.convert(null));
    }

    @Test
    void convert_Byte() {
        // Given
        final Byte input = (byte) 123;

        // When
        final Byte result = converter.convert(input);

        // Then
        assertSame(input, result);
    }

    @Test
    void convert_Number() {
        // Given
        final Integer input = 123;

        // When
        final Byte result = converter.convert(input);

        // Then
        assertEquals((byte) 123, result);
    }

    @Test
    void convert_String() {
        // Given
        final String input = "123";

        // When
        final Byte result = converter.convert(input);

        // Then
        assertEquals((byte) 123, result);
    }

    @Test
    void convert_BlankString() {
        assertNull(converter.convert(""));
        assertNull(converter.convert("  "));
    }

    @Test
    void type() {
        assertEquals(Byte.class, converter.type());
    }

    @Test
    void primitiveType() {
        assertEquals(byte.class, converter.primitiveType());
    }

    @Test
    void sqlTypes() {
        assertArrayEquals(new int[]{Types.TINYINT}, converter.sqlTypes());
    }
}
