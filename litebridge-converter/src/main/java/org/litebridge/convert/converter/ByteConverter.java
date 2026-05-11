package org.litebridge.convert.converter;

import org.jspecify.annotations.Nullable;

import java.sql.Types;

public class ByteConverter extends AbstractNumberConverter<Byte> implements SqlConverter<Byte> {

    private static final int[] SQL_TYPES = new int[]{Types.TINYINT};

    @Override
    protected Byte convertNumber(final Number value) {
        return value.byteValue();
    }

    @Override
    protected Byte convertString(final String value) {
        return Byte.valueOf(value);
    }

    @Override
    public int[] sqlTypes() {
        return SQL_TYPES;
    }

    @Override
    public Class<?> type() {
        return Byte.class;
    }

    @Override
    public @Nullable Class<?> primitiveType() {
        return byte.class;
    }
}
