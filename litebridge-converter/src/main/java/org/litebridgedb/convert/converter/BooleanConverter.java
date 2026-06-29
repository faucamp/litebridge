package org.litebridgedb.convert.converter;


import java.sql.Types;

/**
 * A converter for {@link Boolean} values.
 * <p>
 * Handles {@link java.sql.Types#BIT} and {@link java.sql.Types#BOOLEAN} by parsing the value's string representation.
 */
public class BooleanConverter extends AbstractStringParsingConverter<Boolean> implements SqlConverter<Boolean> {

    private static final int[] SQL_TYPES = new int[]{Types.BIT, Types.BOOLEAN};

    @Override
    protected Boolean convertString(final String value) {
        return Boolean.valueOf(value);
    }

    /**
     * Returns the target Java class this converter handles.
     *
     * @return {@link Boolean}.class
     */
    @Override
    public Class<?> type() {
        return Boolean.class;
    }

    /**
     * Returns the primitive counterpart of the target class.
     *
     * @return {@code boolean.class}
     */
    @Override
    public Class<?> primitiveType() {
        return boolean.class;
    }

    /**
     * Returns the SQL types associated with this converter.
     *
     * @return an array containing {@link java.sql.Types#BIT} and {@link java.sql.Types#BOOLEAN}
     */
    @Override
    public int[] sqlTypes() {
        return SQL_TYPES;
    }
}
