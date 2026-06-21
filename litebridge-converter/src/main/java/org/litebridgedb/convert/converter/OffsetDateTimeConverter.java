package org.litebridgedb.convert.converter;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.commons.TimeUtils;

import java.sql.Types;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Objects;

/**
 * A converter for {@link OffsetDateTimeConverter} values.
 * <p>
 * Handles {@link Types#TIMESTAMP}.
 */
public class OffsetDateTimeConverter implements SqlConverter<OffsetDateTime> {

    private static final int[] SQL_TYPES = new int[]{Types.TIMESTAMP_WITH_TIMEZONE};

    @Override
    public @Nullable OffsetDateTime convert(final @Nullable Object value) {
        if (value == null) {
            return null;
        }

        return switch (value) {
            case Date date -> OffsetDateTime.from(date.toInstant());
            case LocalDate localDate -> OffsetDateTime.from(localDate);
            case LocalDateTime localDateTime -> OffsetDateTime.from(localDateTime);
            case Number number -> OffsetDateTime.from(Instant.ofEpochMilli(number.longValue()));
            case OffsetDateTime offsetDateTime -> offsetDateTime;
            case String string -> OffsetDateTime.from(Objects.requireNonNull(TimeUtils.toZonedDateTime(string)));
            case ZonedDateTime zonedDateTime -> OffsetDateTime.from(zonedDateTime);
            default -> throw new IllegalArgumentException("Unsupported type: " + value.getClass().getName());
        };
    }

    /**
     * Returns the target Java class this converter handles.
     *
     * @return {@link OffsetDateTime}.class
     */
    @Override
    public Class<?> type() {
        return OffsetDateTime.class;
    }

    /**
     * Returns the SQL types associated with this converter.
     *
     * @return an array containing {@link Types#TIMESTAMP_WITH_TIMEZONE}
     */
    @Override
    public int[] sqlTypes() {
        return SQL_TYPES;
    }
}
