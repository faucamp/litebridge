package org.litebridge.convert.converter;


import java.sql.Types;

/**
 * Convert to a boolean by parsing the value as a string
 *
 */
public class BooleanConverter extends AbstractStringParsingConverter<Boolean> implements SqlConverter<Boolean> {

    private static final int[] SQL_TYPES = new int[]{Types.BIT, Types.BOOLEAN};

    @Override
    protected Boolean convertString(final String value) {
        return Boolean.valueOf(value);
    }

    @Override
    public Class<?> type() {
        return Boolean.class;
    }

    @Override
    public int[] sqlTypes() {
        return SQL_TYPES;
    }
}
