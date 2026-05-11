package org.litebridge.convert.converter;

import java.sql.Types;

public class ShortConverter extends AbstractNumberConverter<Short> implements SqlConverter<Short> {

    private static final int[] SQL_TYPES = new int[]{Types.SMALLINT};

    @Override
    protected Short convertNumber(final Number value) {
        return value.shortValue();
    }

    @Override
    protected Short convertString(final String value) {
        return Short.valueOf(value);
    }

    @Override
    public int[] sqlTypes() {
        return SQL_TYPES;
    }

    @Override
    public Class<?> type() {
        return Short.class;
    }
}
