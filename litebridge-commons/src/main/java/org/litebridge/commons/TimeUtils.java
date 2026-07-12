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

public final class TimeUtils {

    @SuppressWarnings("SuspiciousDateFormat")
    private static final DateTimeFormatter LOOSE_ISO_DATE_TIME_ZONE_PARSER = DateTimeFormatter.ofPattern("[yyyyMMdd][yyyy-MM-dd][yyyy-DDD]['T'[HHmmss][HHmm][HH:mm:ss][HH:mm][.SSSSSSSSS][.SSSSSSS][.SSSSSS][.SSSSS][.SSSS][.SSS][.SS][.S][Z]][' 'HH:mm:ss][.SSS][OOOO][O][z][XXXXX][XXXX]['['VV']']");
    private static final DateTimeFormatter DATE_FORMAT_WITH_SLASHES = DateTimeFormatter.ofPattern("[yyyy/MM/dd][dd/MM/yyyy][M[M]/d[d]/yyyy][M[M]/d[d]/yy][, HH:mm[:ss]]");
    private static final Map<String, DateTimeFormatter> CACHED_DATE_TIME_FORMATTERS = new ConcurrentHashMap<>();

    private TimeUtils() {
    }

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

    public static @Nullable ZonedDateTime toZonedDateTime(final @Nullable Date requestedDate) {
        if (requestedDate == null) {
            return null;
        }

        return ZonedDateTime.ofInstant(requestedDate.toInstant(), ZoneId.systemDefault());
    }

    public static @Nullable ZonedDateTime toZonedDateTime(final @Nullable LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }

        return ZonedDateTime.of(localDateTime, ZoneId.systemDefault());
    }

    public static @Nullable ZonedDateTime toZonedDateTime(final @Nullable LocalDate localDate) {
        if (localDate == null) {
            return null;
        }

        return localDate.atStartOfDay().atZone(ZoneId.systemDefault());
    }

    public static @Nullable Date toDate(final @Nullable ZonedDateTime zonedDateTime) {
        if (zonedDateTime == null) {
            return null;
        }

        return Date.from(zonedDateTime.toInstant());
    }

    public static @Nullable Date toDate(final @Nullable LocalDate localDate) {
        if (localDate == null) {
            return null;
        }

        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public static @Nullable Date toDate(final @Nullable LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }

        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static @Nullable LocalDate toLocalDate(final @Nullable ZonedDateTime zonedDateTime) {
        if (zonedDateTime == null) {
            return null;
        }

        return zonedDateTime.toLocalDate();
    }

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

    public static @Nullable LocalDateTime toLocalDateTime(final @Nullable ZonedDateTime zonedDateTime) {
        if (zonedDateTime == null) {
            return null;
        }

        return zonedDateTime.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
    }

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

    public static String format(final Temporal dateToFormat, final String pattern) {
        final DateTimeFormatter dateTimeFormatter = CACHED_DATE_TIME_FORMATTERS.computeIfAbsent(pattern, formatPattern -> DateTimeFormatter.ofPattern(pattern));
        return dateTimeFormatter.format(dateToFormat);
    }
}
