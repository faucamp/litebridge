package org.litebridge.convert.converter;

import java.sql.Date;
import java.sql.Types;

public class SqlDateConverter extends AbstractStringParsingConverter<Date> implements SqlConverter<Date> {

    private static final int[] SQL_TYPES = new int[]{Types.DATE};

    @Override
    protected Date convertString(final String value) {
        return Date.valueOf(value);
    }

    @Override
    public Class<?> type() {
        return Date.class;
    }

    @Override
    public int[] sqlTypes() {
        return SQL_TYPES;
    }
}
