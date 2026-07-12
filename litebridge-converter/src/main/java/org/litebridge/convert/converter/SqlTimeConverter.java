package org.litebridge.convert.converter;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.StringUtils;

import java.sql.Time;
import java.sql.Types;
import java.time.LocalTime;

/**
 * A converter for {@link java.sql.Time} values.
 * <p>
 * Handles {@link java.sql.Types#TIME}.
 */
public class SqlTimeConverter implements SqlConverter<Time> {

    private static final int[] SQL_TYPES = new int[]{Types.TIME};

    /**
     * Converts the given value to a {@link java.sql.Time}.
     * <p>
     * Supports {@link java.sql.Time}, {@link java.time.LocalTime}, and string representations.
     *
     * @param value the value to convert, may be {@code null}
     * @return the converted {@link java.sql.Time}, or {@code null}
     */
    @Override
    public @Nullable Time convert(final @Nullable Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Time time) {
            return time;
        } else if (value instanceof LocalTime localTime) {
            return Time.valueOf(localTime);
        }

        final String valueStr = value.toString();

        if (StringUtils.isBlank(valueStr)) {
            return null;
        } else {
            return Time.valueOf(valueStr);
        }
    }

    /**
     * Returns the target Java class this converter handles.
     *
     * @return {@link java.sql.Time}.class
     */
    @Override
    public Class<?> type() {
        return Time.class;
    }

    /**
     * Returns the SQL types associated with this converter.
     *
     * @return an array containing {@link java.sql.Types#TIME}
     */
    @Override
    public int[] sqlTypes() {
        return SQL_TYPES;
    }
}
