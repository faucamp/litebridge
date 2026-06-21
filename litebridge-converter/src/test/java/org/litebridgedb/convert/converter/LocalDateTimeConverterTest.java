package org.litebridgedb.convert.converter;

import org.junit.jupiter.api.Test;
import org.litebridgedb.commons.TimeUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class LocalDateTimeConverterTest {

    private final LocalDateTimeConverter converter = new LocalDateTimeConverter();

    @Test
    void convert_null() {
        // When
        final LocalDateTime result = converter.convert(null);

        // Then
        assertNull(result);
    }

    @Test
    void convert_date() {
        // Given
        final Date value = Date.from(Instant.parse("2025-12-31T08:44:00Z"));

        // When
        final LocalDateTime result = converter.convert(value);

        // Then
        assertEquals(TimeUtils.toZonedDateTime(value).toLocalDateTime(), result);
    }

    @Test
    void convert_localDateTime() {
        // Given
        final LocalDateTime value = LocalDateTime.of(2025, 12, 31, 10, 44);

        // When
        final LocalDateTime result = converter.convert(value);

        // Then
        assertSame(value, result);
    }

    @Test
    void convert_localDate() {
        // Given
        final LocalDate value = LocalDate.of(2025, 12, 31);

        // When
        final LocalDateTime result = converter.convert(value);

        // Then
        assertEquals(value.atStartOfDay(), result);
    }

    @Test
    void convert_number() {
        // Given
        final long value = Instant.parse("2025-12-31T08:44:00Z").toEpochMilli();

        // When
        final LocalDateTime result = converter.convert(value);

        // Then
        assertEquals(Instant.ofEpochMilli(value).atZone(ZoneId.systemDefault()).toLocalDateTime(), result);
    }

    @Test
    void convert_offsetDateTime() {
        // Given
        final OffsetDateTime value = OffsetDateTime.parse("2025-12-31T10:44:00+02:00");

        // When
        final LocalDateTime result = converter.convert(value);

        // Then
        assertEquals(value.toLocalDateTime(), result);
    }

    @Test
    void convert_zonedDateTime() {
        // Given
        final ZonedDateTime value = ZonedDateTime.parse("2025-12-31T10:44:00+02:00");

        // When
        final LocalDateTime result = converter.convert(value);

        // Then
        assertEquals(TimeUtils.toLocalDateTime("2025-12-31T10:44:00"), result);
    }

    @Test
    void convert_string() {
        // Given
        final String value = "2025-12-31T10:44:00+02:00";

        // When
        final LocalDateTime result = converter.convert(value);

        // Then
        assertEquals(TimeUtils.toLocalDateTime(value), result);
    }

    @Test
    void type() {
        // When
        final Class<?> result = converter.type();

        // Then
        assertEquals(LocalDateTime.class, result);
    }
}