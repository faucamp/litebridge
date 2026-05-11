package org.litebridge.convert.converter;

import org.jspecify.annotations.Nullable;

import java.sql.Types;

/**
 * A converter for {@link Byte} values.
 * <p>
 * Handles {@link java.sql.Types#TINYINT}.
 */
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

    /**
     * Returns the SQL types associated with this converter.
     *
     * @return an array containing {@link java.sql.Types#TINYINT}
     */
    @Override
    public int[] sqlTypes() {
        return SQL_TYPES;
    }

    /**
     * Returns the target Java class this converter handles.
     *
     * @return {@link Byte}.class
     */
    @Override
    public Class<?> type() {
        return Byte.class;
    }

    /**
     * Returns the primitive counterpart of the target class.
     *
     * @return {@code byte.class}
     */
    @Override
    public @Nullable Class<?> primitiveType() {
        return byte.class;
    }
}
