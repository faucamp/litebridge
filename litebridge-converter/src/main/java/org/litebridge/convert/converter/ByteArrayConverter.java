package org.litebridge.convert.converter;

import org.jspecify.annotations.Nullable;

import java.sql.Types;

/**
 * A converter for {@code byte[]} values.
 * <p>
 * Handles {@link java.sql.Types#BINARY}, {@link java.sql.Types#VARBINARY}, and {@link java.sql.Types#LONGVARBINARY}.
 */
public class ByteArrayConverter implements SqlConverter<byte[]> {

    private static final int[] SQL_TYPES = new int[]{Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY};

    /**
     * Converts the given value to a {@code byte[]}.
     * <p>
     * Supports {@code byte[]} and arrays of {@link Number} instances.
     *
     * @param value the value to convert, may be {@code null}
     * @return the converted {@code byte[]}, or {@code null}
     * @throws IllegalArgumentException if the value cannot be converted to {@code byte[]}
     */
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

    /**
     * Returns the target Java class this converter handles.
     *
     * @return {@code byte[].class}
     */
    @Override
    public Class<?> type() {
        return byte[].class;
    }

    /**
     * Returns the SQL types associated with this converter.
     *
     * @return an array containing {@link java.sql.Types#BINARY}, {@link java.sql.Types#VARBINARY}, and {@link java.sql.Types#LONGVARBINARY}
     */
    @Override
    public int[] sqlTypes() {
        return SQL_TYPES;
    }
}
