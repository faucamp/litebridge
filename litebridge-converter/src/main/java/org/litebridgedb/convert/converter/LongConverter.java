package org.litebridgedb.convert.converter;

import org.jspecify.annotations.Nullable;

import java.sql.Types;

/**
 * A converter for {@link Long} values.
 * <p>
 * Handles {@link java.sql.Types#BIGINT}.
 */
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

    /**
     * Returns the target Java class this converter handles.
     *
     * @return {@link Long}.class
     */
    @Override
    public Class<?> type() {
        return Long.class;
    }

    /**
     * Returns the primitive counterpart of the target class.
     *
     * @return {@code long.class}
     */
    @Override
    public @Nullable Class<?> primitiveType() {
        return long.class;
    }

    /**
     * Returns the SQL types associated with this converter.
     *
     * @return an array containing {@link java.sql.Types#BIGINT}
     */
    @Override
    public int[] sqlTypes() {
        return SQL_TYPES;
    }
}
