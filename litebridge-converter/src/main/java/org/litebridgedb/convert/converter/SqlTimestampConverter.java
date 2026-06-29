package org.litebridgedb.convert.converter;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.commons.TimeUtils;

import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Objects;

/**
 * A converter for {@link java.sql.Timestamp} values.
 * <p>
 * Handles {@link java.sql.Types#TIMESTAMP}.
 */
public class SqlTimestampConverter implements SqlConverter<Timestamp> {

    private static final int[] SQL_TYPES = new int[]{Types.TIMESTAMP};

    /**
     * Converts the given value to a {@link java.sql.Timestamp}.
     * <p>
     * Supports various date/time types:
     * <ul>
     *     <li>{@link java.time.ZonedDateTime}</li>
     *     <li>{@link java.time.LocalDateTime}</li>
     *     <li>{@link java.time.LocalDate}</li>
     *     <li>{@link java.util.Date}</li>
     *     <li>{@link java.lang.Number} (treated as milliseconds since epoch)</li>
     *     <li>{@link java.lang.String} (parsed as date/time)</li>
     * </ul>
     *
     * @param value the value to convert, may be {@code null}
     * @return the converted {@link java.sql.Timestamp}, or {@code null}
     * @throws IllegalArgumentException if the value type is not supported
     */
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

    /**
     * Returns the target Java class this converter handles.
     *
     * @return {@link java.sql.Timestamp}.class
     */
    @Override
    public Class<?> type() {
        return Timestamp.class;
    }

    /**
     * Returns the SQL types associated with this converter.
     *
     * @return an array containing {@link java.sql.Types#TIMESTAMP}
     */
    @Override
    public int[] sqlTypes() {
        return SQL_TYPES;
    }
}
