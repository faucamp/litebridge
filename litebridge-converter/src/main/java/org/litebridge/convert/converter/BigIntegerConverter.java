package org.litebridge.convert.converter;

import org.jspecify.annotations.Nullable;

import java.math.BigInteger;
import java.sql.Types;

public class BigIntegerConverter implements Converter<BigInteger> {

    private static final int[] SQL_TYPES = new int[]{Types.NUMERIC, Types.DECIMAL};

    @Override
    public @Nullable BigInteger convert(final @Nullable Object value) {
        if (value == null) {
            return null;
        }

        return switch (value) {
            case BigInteger bigInteger -> bigInteger;
            case Long longValue -> BigInteger.valueOf(longValue);
            case Integer intValue -> BigInteger.valueOf(intValue);
            case Short shortValue -> BigInteger.valueOf(shortValue);
            case Byte byteValue -> BigInteger.valueOf(byteValue);
            case Double doubleValue -> BigInteger.valueOf(doubleValue.longValue());
            case Float floatValue -> BigInteger.valueOf(floatValue.longValue());
            default -> new BigInteger(value.toString());
        };
    }

    @Override
    public Class<?> type() {
        return BigInteger.class;
    }
}
