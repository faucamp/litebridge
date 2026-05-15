package org.litebridgedb.convert.converter;

import org.jspecify.annotations.Nullable;

import java.sql.Types;

/**
 * A converter for {@link Integer} values.
 * <p>
 * Handles {@link java.sql.Types#INTEGER}.
 */
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

    /**
     * Returns the target Java class this converter handles.
     *
     * @return {@link Integer}.class
     */
    @Override
    public Class<?> type() {
        return Integer.class;
    }

    /**
     * Returns the primitive counterpart of the target class.
     *
     * @return {@code int.class}
     */
    @Override
    public @Nullable Class<?> primitiveType() {
        return int.class;
    }

    /**
     * Returns the SQL types associated with this converter.
     *
     * @return an array containing {@link java.sql.Types#INTEGER}
     */
    @Override
    public int[] sqlTypes() {
        return SQL_TYPES;
    }
}
