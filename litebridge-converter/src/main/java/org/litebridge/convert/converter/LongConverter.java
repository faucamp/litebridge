package org.litebridge.convert.converter;

import org.jspecify.annotations.Nullable;

import java.sql.Types;

public class LongConverter extends AbstractNumberConverter<Long> implements SqlConverter<Long> {

    private static final int[] SQL_TYPES = new int[]{Types.BIGINT};

    @Override
    protected Long convertNumber(final Number value) {
        return value.longValue();
    }

    @Override
    protected Long convertString(final String value) {
        return Long.valueOf(value);
    }

    @Override
    public Class<?> type() {
        return Long.class;
    }

    @Override
    public @Nullable Class<?> primitiveType() {
        return long.class;
    }

    @Override
    public int[] sqlTypes() {
        return SQL_TYPES;
    }
}
