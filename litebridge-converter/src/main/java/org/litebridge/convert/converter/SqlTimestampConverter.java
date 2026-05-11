package org.litebridge.convert.converter;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.TimeUtils;

import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Objects;

public class SqlTimestampConverter implements SqlConverter<Timestamp> {

    private static final int[] SQL_TYPES = new int[]{Types.TIMESTAMP};

    @Override
    public @Nullable Timestamp convert(final @Nullable Object value) {
        if (value == null) {
            return null;
        }

        return switch (value) {
            case ZonedDateTime zonedDateTime -> Timestamp.from(zonedDateTime.toInstant());
            case LocalDateTime localDateTime -> Timestamp.from(TimeUtils.toZonedDateTime(localDateTime).toInstant());
            case LocalDate localDate -> Timestamp.valueOf(localDate.atStartOfDay());
            case Date date -> new Timestamp(date.getTime());
            case Number number -> new Timestamp(number.longValue());
            case String string -> Timestamp.from(Objects.requireNonNull(TimeUtils.toZonedDateTime(string)).toInstant());
            default -> throw new IllegalArgumentException("Unsupported type: " + value.getClass().getName());
        };
    }

    @Override
    public Class<?> type() {
        return Timestamp.class;
    }

    @Override
    public int[] sqlTypes() {
        return SQL_TYPES;
    }
}
