package org.litebridge.convert.converter;

import java.sql.Types;

public class FloatConverter extends AbstractNumberConverter<Float> implements SqlConverter<Float> {

    private static final int[] SQL_TYPES = new int[]{Types.REAL};

    @Override
    protected Float convertNumber(final Number value) {
        return value.floatValue();
    }

    @Override
    protected Float convertString(final String value) {
        return Float.valueOf(value);
    }

    @Override
    public int[] sqlTypes() {
        return SQL_TYPES;
    }

    @Override
    public Class<?> type() {
        return Float.class;
    }
}
