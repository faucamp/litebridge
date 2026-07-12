package org.litebridge.convert.converter;

import org.junit.jupiter.api.Test;
import org.litebridge.commons.TimeUtils;

import java.sql.Types;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OffsetDateTimeConverterTest {

    private final OffsetDateTimeConverter converter = new OffsetDateTimeConverter();

    @Test
    void convert_null() {
        // When
        final OffsetDateTime result = converter.convert(null);

        // Then
        assertNull(result);
    }

    @Test
    void convert_date() {
        // Given
        final Date value = Date.from(Instant.parse("2025-12-31T08:44:00Z"));

        // When/Then
        assertThrows(DateTimeException.class, () -> converter.convert(value));
    }

    @Test
    void convert_localDate() {
        // Given
        final LocalDate value = LocalDate.of(2025, 12, 31);

        // When/Then
        assertThrows(DateTimeException.class, () -> converter.convert(value));
    }

    @Test
    void convert_localDateTime() {
        // Given
        final LocalDateTime value = LocalDateTime.of(2025, 12, 31, 10, 44);

        // When/Then
        assertThrows(DateTimeException.class, () -> converter.convert(value));
    }

    @Test
    void convert_number() {
        // Given
        final long value = Instant.parse("2025-12-31T08:44:00Z").toEpochMilli();

        // When/Then
        assertThrows(DateTimeException.class, () -> converter.convert(value));
    }

    @Test
    void convert_offsetDateTime() {
        // Given
        final OffsetDateTime value = OffsetDateTime.parse("2025-12-31T10:44:00+02:00");

        // When
        final OffsetDateTime result = converter.convert(value);

        // Then
        assertSame(value, result);
    }

    @Test
    void convert_string() {
        // Given
        final String value = "2025-12-31T10:44:00+02:00";

        // When
        final OffsetDateTime result = converter.convert(value);

        // Then
        assertEquals(OffsetDateTime.from(TimeUtils.toZonedDateTime(value)), result);
    }

    @Test
    void convert_zonedDateTime() {
        // Given
        final ZonedDateTime value = ZonedDateTime.parse("2025-12-31T10:44:00+02:00");

        // When
        final OffsetDateTime result = converter.convert(value);

        // Then
        assertEquals(OffsetDateTime.from(value), result);
    }

    @Test
    void convert_unsupportedType() {
        // Given
        final Object value = new Object();

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> converter.convert(value));
    }

    @Test
    void type() {
        // When
        final Class<?> result = converter.type();

        // Then
        assertEquals(OffsetDateTime.class, result);
    }

    @Test
    void sqlTypes() {
        // When
        final int[] result = converter.sqlTypes();

        // Then
        assertArrayEquals(new int[]{Types.TIMESTAMP_WITH_TIMEZONE}, result);
    }
}