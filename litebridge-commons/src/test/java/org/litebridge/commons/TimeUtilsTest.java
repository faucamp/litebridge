package org.litebridge.commons;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZonedDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimeUtilsTest {

    @Test
    void toZonedDateTime_string() {
        // Given
        final String inputDate = "2011-12-03T10:15:30+01:00";

        // When
        final ZonedDateTime result = TimeUtils.toZonedDateTime(inputDate);

        // Then
        assertNotNull(result);
        assertEquals(2011, result.getYear());
        assertEquals(12, result.getMonthValue());
        assertEquals(3, result.getDayOfMonth());
        assertEquals(10, result.getHour());
        assertEquals(15, result.getMinute());
        assertEquals(30, result.getSecond());
    }

    @Test
    void toZonedDateTime_string_dateOnly() {
        // Given
        final String inputDate = "2023-11-27";

        // When
        final ZonedDateTime result = TimeUtils.toZonedDateTime(inputDate);

        // Then
        assertNotNull(result);
        assertEquals(2023, result.getYear());
        assertEquals(11, result.getMonthValue());
        assertEquals(27, result.getDayOfMonth());
        assertEquals(0, result.getHour());
        assertEquals(0, result.getMinute());
        assertEquals(0, result.getSecond());
    }

    @Test
    void toZonedDateTime_string_dateOnly_yyyyMMdd() {
        // Given
        final String inputDate = "20231127";

        // When
        final ZonedDateTime result = TimeUtils.toZonedDateTime(inputDate);

        // Then
        assertNotNull(result);
        assertEquals(2023, result.getYear());
        assertEquals(11, result.getMonthValue());
        assertEquals(27, result.getDayOfMonth());
    }

    @Test
    void toZonedDateTime_string_timeZoneWithoutColon() {
        // Given
        final String inputDate = "2023-10-01T23:59:59.000+0200";

        // When
        final ZonedDateTime result = TimeUtils.toZonedDateTime(inputDate);

        // Then
        assertNotNull(result);
        assertEquals(2023, result.getYear());
        assertEquals(10, result.getMonthValue());
        assertEquals(1, result.getDayOfMonth());
        assertEquals(23, result.getHour());
        assertEquals(59, result.getMinute());
        assertEquals(59, result.getSecond());
    }

    @Test
    void toZonedDateTime_string_descriptive() {
        // Given
        final String inputDate = "2016-10-27T16:36:08GMT+1[Europe/Paris]";

        // When
        final ZonedDateTime result = TimeUtils.toZonedDateTime(inputDate);

        // Then
        assertNotNull(result);
        assertEquals(2016, result.getYear());
        assertEquals(10, result.getMonthValue());
        assertEquals(27, result.getDayOfMonth());
    }

    @Test
    void toZonedDateTime_string_dateSpaceTime() {
        // Given
        final String inputDate = "2023-08-23 05:02:23";

        // When
        final ZonedDateTime result = TimeUtils.toZonedDateTime(inputDate);

        // Then
        assertNotNull(result);
        assertEquals(2023, result.getYear());
        assertEquals(8, result.getMonthValue());
        assertEquals(23, result.getDayOfMonth());
        assertEquals(5, result.getHour());
        assertEquals(2, result.getMinute());
        assertEquals(23, result.getSecond());
    }

    @Test
    void toZonedDateTime_string_dateSpaceTimeMilliseconds() {
        // Given
        final String inputDate = "2025-10-29 01:53:49.378";

        // When
        final ZonedDateTime result = TimeUtils.toZonedDateTime(inputDate);

        // Then
        assertNotNull(result);
        assertEquals(2025, result.getYear());
        assertEquals(10, result.getMonthValue());
        assertEquals(29, result.getDayOfMonth());
        assertEquals(1, result.getHour());
        assertEquals(53, result.getMinute());
        assertEquals(49, result.getSecond());
        assertEquals(378000000, result.getNano());
    }

    @Test
    void toZonedDateTime_string_yyyyMMdd_forwardSlashes() {
        // Given
        final String inputDate = "2023/05/26";

        // When
        final ZonedDateTime result = TimeUtils.toZonedDateTime(inputDate);

        // Then
        assertNotNull(result);
        assertEquals(2023, result.getYear());
        assertEquals(5, result.getMonthValue());
        assertEquals(26, result.getDayOfMonth());
    }

    @Test
    void toZonedDateTime_string_MMddYY_forwardSlashes() {
        // Given
        final String inputDate = "5/26/23";

        // When
        final ZonedDateTime result = TimeUtils.toZonedDateTime(inputDate);

        // Then
        assertNotNull(result);
        assertEquals(2023, result.getYear());
        assertEquals(5, result.getMonthValue());
        assertEquals(26, result.getDayOfMonth());
    }

    @Test
    void toZonedDateTime_string_yyyyMMdd_forwardSlashes_commaSpaceTime() {
        // Given
        final String inputDate = "2025/03/23, 06:45";

        // When
        final ZonedDateTime result = TimeUtils.toZonedDateTime(inputDate);

        // Then
        assertNotNull(result);
        assertEquals(2025, result.getYear());
        assertEquals(3, result.getMonthValue());
        assertEquals(23, result.getDayOfMonth());
        assertEquals(6, result.getHour());
        assertEquals(45, result.getMinute());
        assertEquals(0, result.getSecond());
    }

    @Test
    void toZonedDateTime_string_null() {
        assertNull(TimeUtils.toZonedDateTime((String) null));
    }

    @Test
    void toZonedDateTime_localDate() {
        // Given
        final LocalDate localDate = LocalDate.now();

        // When
        final ZonedDateTime result = TimeUtils.toZonedDateTime(localDate);

        // Then
        assertEquals(localDate.getYear(), result.getYear());
        assertEquals(localDate.getMonthValue(), result.getMonthValue());
        assertEquals(localDate.getDayOfMonth(), result.getDayOfMonth());
    }

    @Test
    void toZonedDateTime_localDate_null() {
        assertNull(TimeUtils.toZonedDateTime((LocalDate) null));
    }

    @Test
    void toZonedDateTime_date() {
        ZonedDateTime result = TimeUtils.toZonedDateTime(new Date());
        assertNotNull(result);
    }

    @Test
    void toZonedDateTime_date_null() {
        assertNull(TimeUtils.toZonedDateTime((Date) null));
    }

    @Test
    void toZonedDateTime_localDateTime() {
        // Given
        final LocalDateTime localDateTime = LocalDateTime.now();

        // When
        final ZonedDateTime result = TimeUtils.toZonedDateTime(localDateTime);

        // Then
        assertEquals(localDateTime.getYear(), result.getYear());
        assertEquals(localDateTime.getMonthValue(), result.getMonthValue());
        assertEquals(localDateTime.getDayOfMonth(), result.getDayOfMonth());
        assertEquals(localDateTime.getHour(), result.getHour());
        assertEquals(localDateTime.getMinute(), result.getMinute());
        assertEquals(localDateTime.getSecond(), result.getSecond());
    }

    @Test
    void toZonedDateTime_localDateTime_null() {
        assertNull(TimeUtils.toZonedDateTime((LocalDateTime) null));
    }

    @Test
    void toLocalDate_string() {
        // Given
        final String inputDate = "2023-11-27";

        // When
        final LocalDate result = TimeUtils.toLocalDate(inputDate);

        // Then
        assertNotNull(result);
        assertEquals(2023, result.getYear());
        assertEquals(11, result.getMonthValue());
        assertEquals(27, result.getDayOfMonth());
    }

    @Test
    void toLocalDate_string_yyyyMMdd() {
        // Given
        final String inputDate = "20231127";

        // When
        final LocalDate result = TimeUtils.toLocalDate(inputDate);

        // Then
        assertNotNull(result);
        assertEquals(2023, result.getYear());
        assertEquals(11, result.getMonthValue());
        assertEquals(27, result.getDayOfMonth());
    }

    @Test
    void toLocalDate_string_yyyyMMdd_forwardSlashes() {
        // Given
        final String inputDate = "2023/05/26";

        // When
        final LocalDate result = TimeUtils.toLocalDate(inputDate);

        // Then
        assertNotNull(result);
        assertEquals(2023, result.getYear());
        assertEquals(5, result.getMonthValue());
        assertEquals(26, result.getDayOfMonth());
    }

    @Test
    void toLocalDate_string_stringWithTime() {
        // Given
        final String inputDate = "2011-12-03T10:15:30+01:00";

        // When
        final LocalDate result = TimeUtils.toLocalDate(inputDate);

        // Then
        assertNotNull(result);
        assertEquals(2011, result.getYear());
        assertEquals(12, result.getMonthValue());
        assertEquals(3, result.getDayOfMonth());
    }

    @Test
    void toLocalDate_string_null() {
        assertNull(TimeUtils.toLocalDate((String) null));
    }

    @Test
    void toLocalDate_zonedDateTime() {
        // Given
        final ZonedDateTime zonedDateTime = ZonedDateTime.now();

        // When
        final LocalDate result = TimeUtils.toLocalDate(zonedDateTime);

        // Then
        assertEquals(zonedDateTime.toLocalDate(), result);
    }

    @Test
    void toLocalDate_zonedDateTime_null() {
        assertNull(TimeUtils.toLocalDate((ZonedDateTime) null));
    }

    @Test
    void toLocalDateTime_zonedDateTime() {
        // Given
        ZonedDateTime zonedDateTime = ZonedDateTime.now();

        // When
        final LocalDateTime result = TimeUtils.toLocalDateTime(zonedDateTime);

        // Then
        assertEquals(zonedDateTime.getYear(), result.getYear());
        assertEquals(zonedDateTime.getMonth(), result.getMonth());
        assertEquals(zonedDateTime.getDayOfMonth(), result.getDayOfMonth());
        assertEquals(zonedDateTime.getHour(), result.getHour());
        assertEquals(zonedDateTime.getMinute(), result.getMinute());
        assertEquals(zonedDateTime.getSecond(), result.getSecond());
    }

    /**
     * Converts a ZonedDateTime in timezone GMT to LocalDateTime in the system timezone (GMT+2)
     */
    @Test
    void toLocalDateTime_zonedDateTime_gmtToSast() {
        // Given
        ZonedDateTime zonedDateTime = TimeUtils.toZonedDateTime("2023-05-26T12:00:00Z");

        // When
        final LocalDateTime result = TimeUtils.toLocalDateTime(zonedDateTime);

        // Then
        assertEquals(2023, result.getYear());
        assertEquals(Month.MAY, result.getMonth());
        assertEquals(26, result.getDayOfMonth());
        assertEquals(14, result.getHour());
        assertEquals(0, result.getMinute());
        assertEquals(0, result.getSecond());
    }

    @Test
    void toLocalDateTime_zonedDateTime_null() {
        assertNull(TimeUtils.toLocalDateTime((ZonedDateTime) null));
    }

    @Test
    void toLocalDateTime_string() {
        // Given
        final String inputDate = "2023-11-27";

        // When
        final LocalDateTime result = TimeUtils.toLocalDateTime(inputDate);

        // Then
        assertNotNull(result);
        assertEquals(2023, result.getYear());
        assertEquals(11, result.getMonthValue());
        assertEquals(27, result.getDayOfMonth());
        assertEquals(0, result.getHour());
        assertEquals(0, result.getMinute());
        assertEquals(0, result.getSecond());
    }

    @Test
    void toLocalDateTime_string_null() {
        assertNull(TimeUtils.toLocalDateTime((String) null));
    }

    @Test
    void toDate_localDateTime() {
        // Given
        final LocalDateTime localDateTime = LocalDateTime.now();

        // When
        final Date result = TimeUtils.toDate(localDateTime);

        // Then
        assertNotNull(result);
    }

    @Test
    void toDate_localDateTime_null() {
        assertNull(TimeUtils.toDate((LocalDateTime) null));
    }

    @Test
    void toDate_zonedDateTime() {
        // Given
        final ZonedDateTime zonedDateTime = ZonedDateTime.now();

        // When
        final Date result = TimeUtils.toDate(zonedDateTime);

        // Then
        assertNotNull(result);
    }

    @Test
    void toDate_zonedDateTime_null() {
        assertNull(TimeUtils.toDate((ZonedDateTime) null));
    }

    @Test
    void toDate_localDate() {
        // Given
        final LocalDate localDate = LocalDate.now();

        // When
        final Date result = TimeUtils.toDate(localDate);

        // Then
        assertNotNull(result);
    }

    @Test
    void toDate_localDate_null() {
        assertNull(TimeUtils.toDate((LocalDate) null));
    }

    @Test
    void format() {
        // Given
        final LocalDateTime localDateTime = LocalDateTime.now();

        // When
        final String result = TimeUtils.format(localDateTime, "yyyy-MM-dd HH:mm:ss");

        // Then
        assertNotNull(result);
        assertTrue(result.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
    }
}