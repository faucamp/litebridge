package org.litebridge.commons;

import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class for converting and formatting date and time objects.
 */
public final class TimeUtils {

    @SuppressWarnings("SuspiciousDateFormat")
    private static final DateTimeFormatter LOOSE_ISO_DATE_TIME_ZONE_PARSER = DateTimeFormatter.ofPattern("[yyyyMMdd][yyyy-MM-dd][yyyy-DDD]['T'[HHmmss][HHmm][HH:mm:ss][HH:mm][.SSSSSSSSS][.SSSSSSS][.SSSSSS][.SSSSS][.SSSS][.SSS][.SS][.S][Z]][' 'HH:mm:ss][.SSS][OOOO][O][z][XXXXX][XXXX]['['VV']']");
    private static final DateTimeFormatter DATE_FORMAT_WITH_SLASHES = DateTimeFormatter.ofPattern("[yyyy/MM/dd][dd/MM/yyyy][M[M]/d[d]/yyyy][M[M]/d[d]/yy][, HH:mm[:ss]]");
    private static final Map<String, DateTimeFormatter> CACHED_DATE_TIME_FORMATTERS = new ConcurrentHashMap<>();

    private TimeUtils() {
    }

    /**
     * Converts a date-time string to a {@link ZonedDateTime}.
     *
     * @param dateStr the date-time string to parse; may be null
     * @return the parsed {@link ZonedDateTime}, or {@code null} if {@code dateStr} is null
     */
    public static @Nullable ZonedDateTime toZonedDateTime(final @Nullable String dateStr) {
        if (dateStr == null) {
            return null;
        }

        TemporalAccessor temporalAccessor;

        try {
            temporalAccessor = LOOSE_ISO_DATE_TIME_ZONE_PARSER.parseBest(dateStr, ZonedDateTime::from, LocalDateTime::from, LocalDate::from);
        } catch (DateTimeParseException ex) {
            // Try a simpler converter, with support for slashes
            temporalAccessor = DATE_FORMAT_WITH_SLASHES.parseBest(dateStr, LocalDateTime::from, LocalDate::from);
        }

        if (temporalAccessor instanceof ZonedDateTime) {
            return ((ZonedDateTime) temporalAccessor);
        } else if (temporalAccessor instanceof LocalDateTime) {
            return ((LocalDateTime) temporalAccessor).atZone(ZoneId.systemDefault());
        } else {
            return ((LocalDate) temporalAccessor).atStartOfDay(ZoneId.systemDefault());
        }
    }

    /**
     * Converts a {@link Date} to a {@link ZonedDateTime} using the system default time zone.
     *
     * @param requestedDate the date to convert; may be null
     * @return the corresponding {@link ZonedDateTime}, or {@code null} if {@code requestedDate} is null
     */
    public static @Nullable ZonedDateTime toZonedDateTime(final @Nullable Date requestedDate) {
        if (requestedDate == null) {
            return null;
        }

        return ZonedDateTime.ofInstant(requestedDate.toInstant(), ZoneId.systemDefault());
    }

    /**
     * Converts a {@link LocalDateTime} to a {@link ZonedDateTime} using the system default time zone.
     *
     * @param localDateTime the local date-time to convert; may be null
     * @return the corresponding {@link ZonedDateTime}, or {@code null} if {@code localDateTime} is null
     */
    public static @Nullable ZonedDateTime toZonedDateTime(final @Nullable LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }

        return ZonedDateTime.of(localDateTime, ZoneId.systemDefault());
    }

    /**
     * Converts a {@link LocalDate} to a {@link ZonedDateTime} at the start of the day in the system default time zone.
     *
     * @param localDate the local date to convert; may be null
     * @return the corresponding {@link ZonedDateTime}, or {@code null} if {@code localDate} is null
     */
    public static @Nullable ZonedDateTime toZonedDateTime(final @Nullable LocalDate localDate) {
        if (localDate == null) {
            return null;
        }

        return localDate.atStartOfDay().atZone(ZoneId.systemDefault());
    }

    /**
     * Converts a {@link ZonedDateTime} to a {@link Date}.
     *
     * @param zonedDateTime the zoned date-time to convert; may be null
     * @return the corresponding {@link Date}, or {@code null} if {@code zonedDateTime} is null
     */
    public static @Nullable Date toDate(final @Nullable ZonedDateTime zonedDateTime) {
        if (zonedDateTime == null) {
            return null;
        }

        return Date.from(zonedDateTime.toInstant());
    }

    /**
     * Converts a {@link LocalDate} to a {@link Date} at the start of the day in the system default time zone.
     *
     * @param localDate the local date to convert; may be null
     * @return the corresponding {@link Date}, or {@code null} if {@code localDate} is null
     */
    public static @Nullable Date toDate(final @Nullable LocalDate localDate) {
        if (localDate == null) {
            return null;
        }

        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Converts a {@link LocalDateTime} to a {@link Date} in the system default time zone.
     *
     * @param localDateTime the local date-time to convert; may be null
     * @return the corresponding {@link Date}, or {@code null} if {@code localDateTime} is null
     */
    public static @Nullable Date toDate(final @Nullable LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }

        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Converts a {@link ZonedDateTime} to a {@link LocalDate}.
     *
     * @param zonedDateTime the zoned date-time to convert; may be null
     * @return the corresponding {@link LocalDate}, or {@code null} if {@code zonedDateTime} is null
     */
    public static @Nullable LocalDate toLocalDate(final @Nullable ZonedDateTime zonedDateTime) {
        if (zonedDateTime == null) {
            return null;
        }

        return zonedDateTime.toLocalDate();
    }

    /**
     * Converts a date string to a {@link LocalDate}.
     *
     * @param dateStr the date string to parse; may be null
     * @return the parsed {@link LocalDate}, or {@code null} if {@code dateStr} is null
     */
    public static @Nullable LocalDate toLocalDate(final @Nullable String dateStr) {
        if (dateStr == null) {
            return null;
        }

        TemporalAccessor temporalAccessor;

        try {
            temporalAccessor = LOOSE_ISO_DATE_TIME_ZONE_PARSER.parseBest(dateStr, ZonedDateTime::from, LocalDateTime::from, LocalDate::from);
        } catch (DateTimeParseException ex) {
            // Try a simpler converter, with support for slashes
            temporalAccessor = DATE_FORMAT_WITH_SLASHES.parse(dateStr, LocalDate::from);
        }

        if (temporalAccessor instanceof ZonedDateTime) {
            return ((ZonedDateTime) temporalAccessor).toLocalDate();
        } else if (temporalAccessor instanceof LocalDateTime) {
            return ((LocalDateTime) temporalAccessor).toLocalDate();
        } else {
            return (LocalDate) temporalAccessor;
        }
    }

    /**
     * Converts a {@link ZonedDateTime} to a {@link LocalDateTime} in the system default time zone.
     *
     * @param zonedDateTime the zoned date-time to convert; may be null
     * @return the corresponding {@link LocalDateTime}, or {@code null} if {@code zonedDateTime} is null
     */
    public static @Nullable LocalDateTime toLocalDateTime(final @Nullable ZonedDateTime zonedDateTime) {
        if (zonedDateTime == null) {
            return null;
        }

        return zonedDateTime.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * Converts a date-time string to a {@link LocalDateTime}.
     *
     * @param dateStr the date-time string to parse; may be null
     * @return the parsed {@link LocalDateTime}, or {@code null} if {@code dateStr} is null
     */
    public static @Nullable LocalDateTime toLocalDateTime(final @Nullable String dateStr) {
        if (dateStr == null) {
            return null;
        }

        final TemporalAccessor temporalAccessor = LOOSE_ISO_DATE_TIME_ZONE_PARSER.parseBest(dateStr, ZonedDateTime::from, LocalDateTime::from, LocalDate::from);

        if (temporalAccessor instanceof ZonedDateTime) {
            return ((ZonedDateTime) temporalAccessor).toLocalDateTime();
        } else if (temporalAccessor instanceof LocalDateTime) {
            return (LocalDateTime) temporalAccessor;
        } else {
            return ((LocalDate) temporalAccessor).atStartOfDay();
        }
    }

    /**
     * Formats the given temporal object using the specified pattern.
     *
     * @param dateToFormat the temporal object to format
     * @param pattern      the pattern to use for formatting
     * @return the formatted date-time string
     */
    public static String format(final Temporal dateToFormat, final String pattern) {
        final DateTimeFormatter dateTimeFormatter = CACHED_DATE_TIME_FORMATTERS.computeIfAbsent(pattern, formatPattern -> DateTimeFormatter.ofPattern(pattern));
        return dateTimeFormatter.format(dateToFormat);
    }
}
