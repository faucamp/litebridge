package org.litebridge.convert.converter;

import java.sql.Types;

public class IntegerConverter extends AbstractNumberConverter<Integer> implements SqlConverter<Integer> {

    private static final int[] SQL_TYPES = new int[]{Types.INTEGER};

    @Override
    protected Integer convertNumber(final Number value) {
        return value.intValue();
    }

    @Override
    protected Integer convertString(final String value) {
        return Integer.valueOf(value);
    }

    @Override
    public int[] sqlTypes() {
        return SQL_TYPES;
    }

    @Override
    public Class<?> type() {
        return Integer.class;
    }
}
