package org.litebridge.convert.converter;

import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.sql.Types;

public class BigDecimalConverter implements SqlConverter<BigDecimal> {

    private static final int[] SQL_TYPES = new int[]{Types.NUMERIC, Types.DECIMAL};

    @Override
    public @Nullable BigDecimal convert(final @Nullable Object value) {
        if (value == null) {
            return null;
        }

        return switch (value) {
            case BigDecimal bigInteger -> bigInteger;
            case Long longValue -> BigDecimal.valueOf(longValue);
            case Integer intValue -> BigDecimal.valueOf(intValue);
            case Short shortValue -> BigDecimal.valueOf(shortValue);
            case Byte byteValue -> BigDecimal.valueOf(byteValue);
            case Double doubleValue -> BigDecimal.valueOf(doubleValue.longValue());
            case Float floatValue -> BigDecimal.valueOf(floatValue.longValue());
            default -> new BigDecimal(value.toString());
        };
    }

    @Override
    public Class<?> type() {
        return BigDecimal.class;
    }

    @Override
    public int[] sqlTypes() {
        return SQL_TYPES;
    }
}
