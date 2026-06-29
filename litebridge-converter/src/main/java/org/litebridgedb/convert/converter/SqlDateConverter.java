package org.litebridgedb.convert.converter;

import java.sql.Date;
import java.sql.Types;

/**
 * A converter for {@link java.sql.Date} values.
 * <p>
 * Handles {@link java.sql.Types#DATE} by parsing the value's string representation.
 */
public class SqlDateConverter extends AbstractStringParsingConverter<Date> implements SqlConverter<Date> {

    private static final int[] SQL_TYPES = new int[]{Types.DATE};

    @Override
    protected Date convertString(final String value) {
        return Date.valueOf(value);
    }

    /**
     * Returns the target Java class this converter handles.
     *
     * @return {@link java.sql.Date}.class
     */
    @Override
    public Class<?> type() {
        return Date.class;
    }

    /**
     * Returns the SQL types associated with this converter.
     *
     * @return an array containing {@link java.sql.Types#DATE}
     */
    @Override
    public int[] sqlTypes() {
        return SQL_TYPES;
    }
}
