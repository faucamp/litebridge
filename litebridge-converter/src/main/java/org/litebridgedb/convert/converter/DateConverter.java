package org.litebridgedb.convert.converter;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.commons.TimeUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Date;

public class DateConverter implements Converter<Date> {

    @Override
    public @Nullable Date convert(final @Nullable Object value) {
        if (value == null) {
            return null;
        }

        return switch (value) {
            case Date date -> date;
            case LocalDateTime localDateTime -> TimeUtils.toDate(localDateTime);
            case LocalDate localDate -> TimeUtils.toDate(localDate);
            case Number number -> new Date(number.longValue());
            case OffsetDateTime offsetDateTime -> Date.from(offsetDateTime.toInstant());
            case ZonedDateTime zonedDateTime -> TimeUtils.toDate(zonedDateTime);
            default -> TimeUtils.toDate(TimeUtils.toZonedDateTime(value.toString()));
        };
    }

    @Override
    public Class<?> type() {
        return Date.class;
    }
}
