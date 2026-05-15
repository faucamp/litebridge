package org.litebridgedb.convert.converter;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Types;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class BigDecimalConverterTest {

    private final BigDecimalConverter converter = new BigDecimalConverter();

    @Test
    void convert_null() {
        assertNull(converter.convert(null));
    }

    @Test
    void convert_BigDecimal() {
        // Given
        final BigDecimal input = new BigDecimal("123.45");

        // When
        final BigDecimal result = converter.convert(input);

        // Then
        assertSame(input, result);
    }

    @Test
    void convert_Long() {
        // Given
        final Long input = 123L;

        // When
        final BigDecimal result = converter.convert(input);

        // Then
        assertEquals(BigDecimal.valueOf(123L), result);
    }

    @Test
    void convert_Integer() {
        // Given
        final Integer input = 123;

        // When
        final BigDecimal result = converter.convert(input);

        // Then
        assertEquals(BigDecimal.valueOf(123), result);
    }

    @Test
    void convert_Short() {
        // Given
        final Short input = (short) 123;

        // When
        final BigDecimal result = converter.convert(input);

        // Then
        assertEquals(BigDecimal.valueOf(123), result);
    }

    @Test
    void convert_Byte() {
        // Given
        final Byte input = (byte) 123;

        // When
        final BigDecimal result = converter.convert(input);

        // Then
        assertEquals(BigDecimal.valueOf(123), result);
    }

    @Test
    void convert_Double() {
        // Given
        final Double input = 123.45;

        // When
        final BigDecimal result = converter.convert(input);

        // Then
        assertEquals(BigDecimal.valueOf(123L), result);
    }

    @Test
    void convert_Float() {
        // Given
        final Float input = 123.45F;

        // When
        final BigDecimal result = converter.convert(input);

        // Then
        assertEquals(BigDecimal.valueOf(123L), result);
    }

    @Test
    void convert_String() {
        // Given
        final String input = "123.45";

        // When
        final BigDecimal result = converter.convert(input);

        // Then
        assertEquals(new BigDecimal("123.45"), result);
    }

    @Test
    void type() {
        assertEquals(BigDecimal.class, converter.type());
    }

    @Test
    void sqlTypes() {
        assertArrayEquals(new int[]{Types.NUMERIC, Types.DECIMAL}, converter.sqlTypes());
    }
}
