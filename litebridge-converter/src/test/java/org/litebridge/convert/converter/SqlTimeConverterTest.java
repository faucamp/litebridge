package org.litebridge.convert.converter;

import org.junit.jupiter.api.Test;

import java.sql.Time;
import java.sql.Types;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class SqlTimeConverterTest {

    private final SqlTimeConverter converter = new SqlTimeConverter();

    @Test
    void convert_null() {
        assertNull(converter.convert(null));
    }

    @Test
    void convert_Time() {
        // Given
        final Time input = Time.valueOf("12:34:56");

        // When
        final Time result = converter.convert(input);

        // Then
        assertSame(input, result);
    }

    @Test
    void convert_LocalTime() {
        // Given
        final LocalTime input = LocalTime.of(12, 34, 56);

        // When
        final Time result = converter.convert(input);

        // Then
        assertEquals(Time.valueOf("12:34:56"), result);
    }

    @Test
    void convert_String() {
        // Given
        final String input = "12:34:56";

        // When
        final Time result = converter.convert(input);

        // Then
        assertEquals(Time.valueOf("12:34:56"), result);
    }

    @Test
    void convert_BlankString() {
        assertNull(converter.convert(""));
        assertNull(converter.convert("  "));
    }

    @Test
    void type() {
        assertEquals(Time.class, converter.type());
    }

    @Test
    void sqlTypes() {
        assertArrayEquals(new int[]{Types.TIME}, converter.sqlTypes());
    }
}
