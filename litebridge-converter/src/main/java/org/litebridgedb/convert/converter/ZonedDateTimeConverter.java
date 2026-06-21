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

public class ZonedDateTimeConverter implements Converter<ZonedDateTime> {

    @Override
    public @Nullable ZonedDateTime convert(final @Nullable Object value) {
        if (value == null) {
            return null;
        }

        return switch (value) {
            case Date date -> TimeUtils.toZonedDateTime(date);
            case LocalDateTime localDateTime -> TimeUtils.toZonedDateTime(localDateTime);
            case LocalDate localDate -> TimeUtils.toZonedDateTime(localDate);
            case Number number -> Instant.ofEpochMilli(number.longValue()).atZone(ZoneId.systemDefault());
            case OffsetDateTime offsetDateTime -> offsetDateTime.toZonedDateTime();
            case ZonedDateTime zonedDateTime -> zonedDateTime;
            default -> TimeUtils.toZonedDateTime(value.toString());
        };
    }

    @Override
    public Class<?> type() {
        return ZonedDateTime.class;
    }
}
