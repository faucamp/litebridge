package org.litebridge.db.oracle.convert;

import oracle.jdbc.OracleTypes;
import oracle.sql.TIMESTAMPTZ;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.sql.Types;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class OracleOffsetDateTimeConverterTest {

    private final OracleOffsetDateTimeConverter converter = new OracleOffsetDateTimeConverter();

    @Test
    void convert_withOffsetDateTime_delegatesToParentConverter() {
        // Given
        final OffsetDateTime value = OffsetDateTime.parse("2025-12-31T10:44:00+02:00");

        // When
        final OffsetDateTime result = converter.convert(value);

        // Then
        assertSame(value, result);
    }

    @Test
    void convert_withNull_delegatesToParentConverter() {
        // When
        final OffsetDateTime result = converter.convert(null);

        // Then
        assertNull(result);
    }

    @Test
    void convert_withTimestampTz_returnsOffsetDateTime() throws SQLException {
        // Given
        final TIMESTAMPTZ value = mock(TIMESTAMPTZ.class);
        final OffsetDateTime expected = OffsetDateTime.parse("2025-12-31T10:44:00+02:00");

        when(value.toOffsetDateTime()).thenReturn(expected);

        // When
        final OffsetDateTime result = converter.convert(value);

        // Then
        assertSame(expected, result);
        verify(value).toOffsetDateTime();
        verifyNoMoreInteractions(value);
    }

    @Test
    void convert_withTimestampTz_whenToOffsetDateTimeFails_fallsBackToZonedDateTime() throws SQLException {
        // Given
        final TIMESTAMPTZ value = mock(TIMESTAMPTZ.class);
        final ZonedDateTime zonedDateTime = ZonedDateTime.of(
                2025,
                12,
                31,
                10,
                44,
                0,
                0,
                ZoneId.of("Europe/Paris")
        );
        final OffsetDateTime expected = zonedDateTime.toOffsetDateTime();

        when(value.toOffsetDateTime()).thenThrow(new SQLException("primary conversion failed"));
        when(value.toZonedDateTime()).thenReturn(zonedDateTime);

        // When
        final OffsetDateTime result = converter.convert(value);

        // Then
        assertEquals(expected, result);
        verify(value).toOffsetDateTime();
        verify(value).toZonedDateTime();
        verifyNoMoreInteractions(value);
    }

    @Test
    void convert_withTimestampTz_whenBothConversionsFail_throwsIllegalArgumentException() throws SQLException {
        // Given
        final TIMESTAMPTZ value = mock(TIMESTAMPTZ.class);
        final SQLException fallbackException = new SQLException("fallback conversion failed");

        when(value.toOffsetDateTime()).thenThrow(new SQLException("primary conversion failed"));
        when(value.toZonedDateTime()).thenThrow(fallbackException);

        // When
        final IllegalArgumentException result = assertThrows(
                IllegalArgumentException.class,
                () -> converter.convert(value)
        );

        // Then
        assertEquals("Failed to convert TIMESTAMPZ to OffsetDateTime", result.getMessage());
        assertSame(fallbackException, result.getCause());
        verify(value).toOffsetDateTime();
        verify(value).toZonedDateTime();
        verifyNoMoreInteractions(value);
    }

    @Test
    void convert_withUnsupportedType_delegatesToParentConverterAndThrowsIllegalArgumentException() {
        // Given
        final Object value = new Object();

        // When
        final IllegalArgumentException result = assertThrows(
                IllegalArgumentException.class,
                () -> converter.convert(value)
        );

        // Then
        assertTrue(result.getMessage().startsWith("Unsupported type: "));
    }

    @Test
    void sqlTypes_returnsOracleAndStandardTimestampWithTimezoneTypes() {
        // When
        final int[] result = converter.sqlTypes();

        // Then
        assertArrayEquals(
                new int[]{Types.TIMESTAMP_WITH_TIMEZONE, OracleTypes.TIMESTAMPTZ},
                result
        );
    }

    @Test
    void type_returnsOffsetDateTimeClassFromParentConverter() {
        // When
        final Class<?> result = converter.type();

        // Then
        assertSame(OffsetDateTime.class, result);
    }
}