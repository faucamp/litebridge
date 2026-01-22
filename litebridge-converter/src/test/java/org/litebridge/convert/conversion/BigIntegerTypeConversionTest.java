package org.litebridge.convert.conversion;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class BigIntegerTypeConversionTest {

    private final BigIntegerTypeConversion conversion = new BigIntegerTypeConversion();

    @Test
    void getTypeKeys() {
        // When
        final Object[] result = conversion.getTypeKeys();

        // Then
        assertEquals(3, result.length);
        assertEquals(BigInteger.class, result[0]);
        assertEquals(BigInteger.class.getName(), result[1]);
        assertEquals(BigIntegerTypeConversion.TYPE_BIGINTEGER, result[2]);
    }

    @Test
    void convert_null() {
        assertNull(conversion.convert(null));
    }

    @Test
    void convert_BigInteger() {
        // Given
        final BigInteger input = new BigInteger("123456789012345678901234567890");

        // When
        final BigInteger result = conversion.convert(input);

        // Then
        assertSame(input, result);
        assertEquals(new BigInteger("123456789012345678901234567890"), result);
    }

    @Test
    void convert_Long() {
        // Given
        final Long input = 9223372036854775807L;

        // When
        final BigInteger result = conversion.convert(input);

        // Then
        assertEquals(BigInteger.valueOf(9223372036854775807L), result);
    }

    @Test
    void convert_Integer() {
        // Given
        final Integer input = 2147483647;

        // When
        final BigInteger result = conversion.convert(input);

        // Then
        assertEquals(BigInteger.valueOf(2147483647), result);
    }

    @Test
    void convert_Short() {
        // Given
        final Short input = 32767;

        // When
        final BigInteger result = conversion.convert(input);

        // Then
        assertEquals(BigInteger.valueOf(32767), result);
    }

    @Test
    void convert_Byte() {
        // Given
        final Byte input = 127;

        // When
        final BigInteger result = conversion.convert(input);

        // Then
        assertEquals(BigInteger.valueOf(127), result);
    }

    @Test
    void convert_String() {
        // Given
        final String input = "999999999999999999999999999999";

        // When
        final BigInteger result = conversion.convert(input);

        // Then
        assertEquals(new BigInteger("999999999999999999999999999999"), result);
    }

    @Test
    void convert_Double() {
        // Given
        final Double input = 123.0;

        // When
        final BigInteger result = conversion.convert(input);

        // Then
        assertEquals(new BigInteger("123"), result);
    }

    @Test
    void convert_Float() {
        // Given
        final Float input = 123.0F;

        // When
        final BigInteger result = conversion.convert(input);

        // Then
        assertEquals(new BigInteger("123"), result);
    }
}