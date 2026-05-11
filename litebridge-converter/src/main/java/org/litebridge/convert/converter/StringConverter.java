package org.litebridge.convert.converter;

import org.jspecify.annotations.Nullable;

import java.sql.Types;

public class StringConverter implements SqlConverter<String> {

    private static final int[] SQL_TYPES = new int[]{Types.CHAR, Types.VARCHAR, Types.LONGVARCHAR};

    @Override
    public @Nullable String convert(final @Nullable Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof String str) {
            return str;
        }

        final String valueStr;

        if (value.getClass().isArray()) {
            if (value.getClass().getComponentType() == Byte.TYPE) {
                valueStr = new String((byte[]) value);
            } else if (value.getClass().getComponentType() == Character.TYPE) {
                valueStr = new String((char[]) value);
            } else {
                valueStr = value.toString();
            }
        } else {
            valueStr = value.toString();
        }

        return valueStr;
    }

    @Override
    public Class<String> type() {
        return String.class;
    }

    @Override
    public int[] sqlTypes() {
        return SQL_TYPES;
    }
}
