package org.litebridgedb.convert.converter;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.commons.TimeUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

public class LocalDateTimeConverter implements Converter<LocalDateTime> {

    @Override
    public @Nullable LocalDateTime convert(final @Nullable Object value) {
        if (value == null) {
            return null;
        }

        return switch (value) {
            case Date date -> TimeUtils.toZonedDateTime(date).toLocalDateTime();
            case LocalDate localDate -> localDate.atStartOfDay();
            case LocalDateTime localDateTime -> localDateTime;
            case Number number ->
                    Instant.ofEpochMilli(number.longValue()).atZone(ZoneId.systemDefault()).toLocalDateTime();
            case OffsetDateTime offsetDateTime -> offsetDateTime.toLocalDateTime();
            case ZonedDateTime zonedDateTime -> zonedDateTime.toLocalDateTime();
            default -> TimeUtils.toLocalDateTime(value.toString());
        };
    }

    @Override
    public Class<?> type() {
        return LocalDateTime.class;
    }
}
