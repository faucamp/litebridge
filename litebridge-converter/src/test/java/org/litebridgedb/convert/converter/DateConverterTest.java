package org.litebridgedb.convert.converter;

import org.junit.jupiter.api.Test;
import org.litebridgedb.commons.TimeUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class DateConverterTest {

    private final DateConverter converter = new DateConverter();

    @Test
    void convert_null() {
        // When
        final Date result = converter.convert(null);

        // Then
        assertNull(result);
    }

    @Test
    void convert_date() {
        // Given
        final Date value = Date.from(Instant.parse("2025-12-31T08:44:00Z"));

        // When
        final Date result = converter.convert(value);

        // Then
        assertSame(value, result);
    }

    @Test
    void convert_localDateTime() {
        // Given
        final LocalDateTime value = LocalDateTime.of(2025, 12, 31, 10, 44);

        // When
        final Date result = converter.convert(value);

        // Then
        assertEquals(TimeUtils.toDate(value), result);
    }

    @Test
    void convert_localDate() {
        // Given
        final LocalDate value = LocalDate.of(2025, 12, 31);

        // When
        final Date result = converter.convert(value);

        // Then
        assertEquals(TimeUtils.toDate(value), result);
    }

    @Test
    void convert_number() {
        // Given
        final long value = Instant.parse("2025-12-31T08:44:00Z").toEpochMilli();

        // When
        final Date result = converter.convert(value);

        // Then
        assertEquals(new Date(value), result);
    }

    @Test
    void convert_offsetDateTime() {
        // Given
        final OffsetDateTime value = OffsetDateTime.parse("2025-12-31T10:44:00+02:00");

        // When
        final Date result = converter.convert(value);

        // Then
        assertEquals(Date.from(value.toInstant()), result);
    }

    @Test
    void convert_zonedDateTime() {
        // Given
        final ZonedDateTime value = ZonedDateTime.parse("2025-12-31T10:44:00+02:00");

        // When
        final Date result = converter.convert(value);

        // Then
        assertEquals(TimeUtils.toDate(value), result);
    }

    @Test
    void convert_string() {
        // Given
        final String value = "2025-12-31T10:44:00+02:00";

        // When
        final Date result = converter.convert(value);

        // Then
        assertEquals(TimeUtils.toDate(TimeUtils.toZonedDateTime(value)), result);
    }

    @Test
    void type() {
        // When
        final Class<?> result = converter.type();

        // Then
        assertEquals(Date.class, result);
    }
}