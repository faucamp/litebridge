package org.litebridge.convert.converter;

import java.sql.Types;

/**
 * A converter for {@link Double} values.
 * <p>
 * Handles {@link java.sql.Types#FLOAT} and {@link java.sql.Types#DOUBLE}.
 */
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

    /**
     * Returns the target Java class this converter handles.
     *
     * @return {@link Double}.class
     */
    @Override
    public Class<?> type() {
        return Double.class;
    }

    /**
     * Returns the primitive counterpart of the target class.
     *
     * @return {@code double.class}
     */
    @Override
    public Class<?> primitiveType() {
        return double.class;
    }

    /**
     * Returns the SQL types associated with this converter.
     *
     * @return an array containing {@link java.sql.Types#FLOAT} and {@link java.sql.Types#DOUBLE}
     */
    @Override
    public int[] sqlTypes() {
        return SQL_TYPES;
    }
}
