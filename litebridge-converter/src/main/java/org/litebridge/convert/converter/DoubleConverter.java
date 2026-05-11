package org.litebridge.convert.converter;

import org.jspecify.annotations.Nullable;

import java.sql.Types;

public class DoubleConverter extends AbstractNumberConverter<Double> implements SqlConverter<Double> {

    private static final int[] SQL_TYPES = new int[]{Types.FLOAT, Types.DOUBLE};

    @Override
    protected Double convertNumber(final Number value) {
        return value.doubleValue();
    }

    @Override
    protected Double convertString(final String value) {
        return Double.valueOf(value);
    }

    @Override
    public Class<?> type() {
        return Double.class;
    }

    @Override
    public Class<?> primitiveType() {
        return double.class;
    }

    @Override
    public int[] sqlTypes() {
        return SQL_TYPES;
    }
}
