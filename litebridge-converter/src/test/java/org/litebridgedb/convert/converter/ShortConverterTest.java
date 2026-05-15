package org.litebridgedb.convert.converter;

import org.junit.jupiter.api.Test;

import java.sql.Types;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class ShortConverterTest {

    private final ShortConverter converter = new ShortConverter();

    @Test
    void convert_null() {
        assertNull(converter.convert(null));
    }

    @Test
    void convert_Short() {
        // Given
        final Short input = (short) 123;

        // When
        final Short result = converter.convert(input);

        // Then
        assertSame(input, result);
    }

    @Test
    void convert_Number() {
        // Given
        final Integer input = 123;

        // When
        final Short result = converter.convert(input);

        // Then
        assertEquals((short) 123, result);
    }

    @Test
    void convert_String() {
        // Given
        final String input = "123";

        // When
        final Short result = converter.convert(input);

        // Then
        assertEquals((short) 123, result);
    }

    @Test
    void convert_BlankString() {
        assertNull(converter.convert(""));
        assertNull(converter.convert("  "));
    }

    @Test
    void type() {
        assertEquals(Short.class, converter.type());
    }

    @Test
    void primitiveType() {
        assertEquals(short.class, converter.primitiveType());
    }

    @Test
    void sqlTypes() {
        assertArrayEquals(new int[]{Types.SMALLINT}, converter.sqlTypes());
    }
}
