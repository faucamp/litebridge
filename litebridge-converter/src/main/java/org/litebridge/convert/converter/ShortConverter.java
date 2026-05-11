package org.litebridge.convert.converter;

import org.jspecify.annotations.Nullable;

import java.sql.Types;

/**
 * A converter for {@link Short} values.
 * <p>
 * Handles {@link java.sql.Types#SMALLINT}.
 */
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

    /**
     * Returns the target Java class this converter handles.
     *
     * @return {@link Short}.class
     */
    @Override
    public Class<?> type() {
        return Short.class;
    }

    /**
     * Returns the primitive counterpart of the target class.
     *
     * @return {@code short.class}
     */
    @Override
    public @Nullable Class<?> primitiveType() {
        return short.class;
    }

    /**
     * Returns the SQL types associated with this converter.
     *
     * @return an array containing {@link java.sql.Types#SMALLINT}
     */
    @Override
    public int[] sqlTypes() {
        return SQL_TYPES;
    }
}
