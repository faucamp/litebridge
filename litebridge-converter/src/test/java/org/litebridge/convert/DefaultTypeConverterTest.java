package org.litebridge.convert;

import org.junit.jupiter.api.Test;
import org.litebridge.commons.TimeUtils;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.sql.Types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultTypeConverterTest {

    private final DefaultTypeConverter defaultTypeConverter = new DefaultTypeConverter();

    @Test
    void convert() {
        // Given
        final String value = "123";

        // When
        final Integer result = defaultTypeConverter.convert(value, Integer.class);

        // Then
        assertNotNull(result);
        assertEquals(123, result);
    }

    @Test
    void testConvert_sqlType_boolean() {
        // Given
        final String value = "true";

        // When
        final Boolean result = (Boolean) defaultTypeConverter.convert(value, Types.BOOLEAN);

        // Then
        assertNotNull(result);
        assertEquals(true, result);
    }

    @Test
    void testConvert_sqlType_timestamp() {
        // Given
        final String value = "2025-12-31T10:55:00+02:00";

        // When
        final Timestamp result = (Timestamp) defaultTypeConverter.convert(value, Types.TIMESTAMP);

        // Then
        assertNotNull(result);
        assertEquals(Timestamp.from(TimeUtils.toZonedDateTime(value).toInstant()), result);
    }

    @Test
    void testConvert_sqlType_varchar() {
        // Given
        final String value = "123";

        // When
        final String result = (String) defaultTypeConverter.convert(value, Types.VARCHAR);

        // Then
        assertNotNull(result);
        assertEquals("123", result);
    }

    @Test
    void testConvert_sqlType_integer() {
        // Given
        final String value = "123";

        // When
        final Integer result = (Integer) defaultTypeConverter.convert(value, Types.INTEGER);

        // Then
        assertNotNull(result);
        assertEquals(123, result);
    }

    @Test
    void testConvert_sqlType_numeric() {
        // Given
        final String value = "123";

        // When
        final BigDecimal result = (BigDecimal) defaultTypeConverter.convert(value, Types.NUMERIC);

        // Then
        assertNotNull(result);
        assertEquals(123, result.intValue());
    }

    @Test
    void testConvert_sqlType_bigint() {
        // Given
        final String value = "123";

        // When
        final Long result = (Long) defaultTypeConverter.convert(value, Types.BIGINT);

        // Then
        assertNotNull(result);
        assertEquals(123, result.intValue());
    }

    @Test
    void testConvert_sqlType_smallint() {
        // Given
        final String value = "123";

        // When
        final Short result = (Short) defaultTypeConverter.convert(value, Types.SMALLINT);

        // Then
        assertNotNull(result);
        assertEquals((short) 123, result.shortValue());
    }

    @Test
    void testConvert_null() {
        // When
        final Integer result = defaultTypeConverter.convert(null, Integer.class);

        // Then
        assertEquals(null, result);
    }

    @Test
    void testConvert_sqlType_null() {
        // When
        final Integer result = (Integer) defaultTypeConverter.convert(null, Types.INTEGER);

        // Then
        assertEquals(null, result);
    }

    @Test
    void testConvert_class_unsupported() {
        // Given
        final String value = "123";

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> defaultTypeConverter.convert(value, Void.class));
    }

    @Test
    void testConvert_sqlType_unsupported() {
        // Given
        final String value = "123";
        final int unknownType = -999;

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> defaultTypeConverter.convert(value, unknownType));
    }
}