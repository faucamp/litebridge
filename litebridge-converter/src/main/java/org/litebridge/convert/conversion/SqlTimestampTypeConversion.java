package org.litebridge.convert.conversion;

import org.litebridge.commons.TimeUtils;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;

public class SqlTimestampTypeConversion extends com.toddfast.util.convert.conversion.SqlTimestampTypeConversion {

    @Override
    public Timestamp convert(final Object value) {
        if (value == null) {
            return null;
        }

        return switch (value) {
            case ZonedDateTime zonedDateTime -> Timestamp.from(zonedDateTime.toInstant());
            case LocalDateTime localDateTime -> Timestamp.from(TimeUtils.toZonedDateTime(localDateTime).toInstant());
            case LocalDate localDate -> Timestamp.valueOf(localDate.atStartOfDay());
            case Date date -> new Timestamp(date.getTime());
            case Number number -> new Timestamp(number.longValue());
            case String string -> Timestamp.from(TimeUtils.toZonedDateTime(string).toInstant());
            default -> throw new IllegalArgumentException("Unsupported type: " + value.getClass().getName());
        };
    }

}
