package org.litebridge.convert.converter;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.TimeUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * A converter for {@link LocalDate} objects.
 */
public class LocalDateConverter implements Converter<LocalDate> {

    @Override
    public @Nullable LocalDate convert(final @Nullable Object value) {
        if (value == null) {
            return null;
        }

        return switch (value) {
            case Date date -> TimeUtils.toZonedDateTime(date).toLocalDate();
            case LocalDate localDate -> localDate;
            case LocalDateTime localDateTime -> localDateTime.toLocalDate();
            case Number number -> Instant.ofEpochMilli(number.longValue()).atZone(ZoneId.systemDefault()).toLocalDate();
            case OffsetDateTime offsetDateTime -> offsetDateTime.toLocalDate();
            case ZonedDateTime zonedDateTime -> zonedDateTime.toLocalDate();
            default -> TimeUtils.toLocalDate(value.toString());
        };
    }

    @Override
    public Class<?> type() {
        return LocalDate.class;
    }
}
