package org.litebridgedb.convert.converter;

import java.sql.Types;

/**
 * A converter for {@link Float} values.
 * <p>
 * Handles {@link java.sql.Types#REAL}.
 */
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

    /**
     * Returns the target Java class this converter handles.
     *
     * @return {@link Float}.class
     */
    @Override
    public Class<?> type() {
        return Float.class;
    }

    /**
     * Returns the primitive counterpart of the target class.
     *
     * @return {@code float.class}
     */
    @Override
    public Class<?> primitiveType() {
        return float.class;
    }

    /**
     * Returns the SQL types associated with this converter.
     *
     * @return an array containing {@link java.sql.Types#REAL}
     */
    @Override
    public int[] sqlTypes() {
        return SQL_TYPES;
    }
}
