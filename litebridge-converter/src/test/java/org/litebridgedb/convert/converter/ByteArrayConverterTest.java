package org.litebridgedb.convert.converter;

import org.junit.jupiter.api.Test;

import java.sql.Types;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ByteArrayConverterTest {

    private final ByteArrayConverter converter = new ByteArrayConverter();

    @Test
    void convert_null() {
        assertNull(converter.convert(null));
    }

    @Test
    void convert_ByteArray() {
        // Given
        final byte[] input = new byte[]{1, 2, 3};

        // When
        final byte[] result = converter.convert(input);

        // Then
        assertSame(input, result);
    }

    @Test
    void convert_NumberArray() {
        // Given
        final Integer[] input = new Integer[]{1, 2, 3};

        // When
        final byte[] result = converter.convert(input);

        // Then
        assertArrayEquals(new byte[]{1, 2, 3}, result);
    }

    @Test
    void convert_InvalidType() {
        assertThrows(IllegalArgumentException.class, () -> converter.convert("abc"));
    }

    @Test
    void type() {
        assertEquals(byte[].class, converter.type());
    }

    @Test
    void sqlTypes() {
        assertArrayEquals(new int[]{Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY}, converter.sqlTypes());
    }
}
