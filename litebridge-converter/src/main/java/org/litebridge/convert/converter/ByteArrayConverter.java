package org.litebridge.convert.converter;

import org.jspecify.annotations.Nullable;

import java.sql.Types;

public class ByteArrayConverter implements SqlConverter<byte[]> {

    private static final int[] SQL_TYPES = new int[]{Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY};

    @Override
    public @Nullable byte[] convert(final @Nullable Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof byte[] bytes) {
            return bytes;
        } else if (Number[].class.isAssignableFrom(value.getClass())) {
            final Number[] numbers = (Number[]) value;
            final byte[] primitiveBytes = new byte[numbers.length];

            for (int i = 0; i < numbers.length; i++) {
                primitiveBytes[i] = numbers[i].byteValue();
            }

            return primitiveBytes;
        } else {
            throw new IllegalArgumentException("Cannot convert value of type " + value.getClass() + " to byte[]");
        }
    }

    @Override
    public Class<?> type() {
        return byte[].class;
    }

    @Override
    public int[] sqlTypes() {
        return SQL_TYPES;
    }
}
