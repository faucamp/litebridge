package org.litebridge.convert.converter;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.StringUtils;

import java.sql.Time;
import java.sql.Types;
import java.time.LocalTime;

public class SqlTimeConverter implements SqlConverter<Time> {

    private static final int[] SQL_TYPES = new int[]{Types.TIME};

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

    @Override
    public Class<?> type() {
        return Time.class;
    }

    @Override
    public int[] sqlTypes() {
        return SQL_TYPES;
    }
}
