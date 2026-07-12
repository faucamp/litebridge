package org.litebridge.convert.converter;

import org.junit.jupiter.api.Test;
import org.litebridge.commons.TimeUtils;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SqlTimestampConverterTest {

    private final SqlTimestampConverter converter = new SqlTimestampConverter();

    @Test
    void convert_zonedDateTime() {
        // Given
        final ZonedDateTime value = ZonedDateTime.now();

        // When
        final Timestamp result = converter.convert(value);

        // Then
        assertNotNull(result);
        assertEquals(value.toInstant(), result.toInstant());
    }

    @Test
    void convert_localDateTime() {
        // Given
        final LocalDateTime value = LocalDateTime.now();

        // When
        final Timestamp result = converter.convert(value);

        // Then
        assertNotNull(result);
        assertEquals(value.atZone(ZoneId.systemDefault()).toInstant(), result.toInstant());
    }

    @Test
    void convert_localDate() {
        // Given
        final LocalDate value = LocalDate.now();

        // When
        final Timestamp result = converter.convert(value);

        // Then
        assertNotNull(result);
        assertEquals(value.atStartOfDay(ZoneId.systemDefault()).toInstant(), result.toInstant());
    }

    @Test
    void convert_date() {
        // Given
        final Date value = new Date();

        // When
        final Timestamp result = converter.convert(value);

        // Then
        assertNotNull(result);
        assertEquals(value.toInstant(), result.toInstant());
    }

    @Test
    void convert_epoch() {
        // Given
        final long value = System.currentTimeMillis();

        // When
        final Timestamp result = converter.convert(value);

        // Then
        assertNotNull(result);
        assertEquals(value, result.getTime());
    }

    @Test
    void convert_string() {
        // Given
        final String value = "2025-12-31T10:44:00+02:00";

        // When
        final Timestamp result = converter.convert(value);

        // Then
        assertNotNull(result);
        assertEquals(TimeUtils.toZonedDateTime(value).toInstant().toEpochMilli(), result.getTime());
    }

    @Test
    void convert_null() {
        // When
        final Timestamp result = converter.convert(null);

        // Then
        assertNull(result);
    }

    @Test
    void convert_unsupportedType() {
        // Given
        final Object value = new Object();

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> converter.convert(value));
    }
}