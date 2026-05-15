package org.litebridgedb.convert.converter;

import org.jspecify.annotations.Nullable;

import java.math.BigInteger;

/**
 * A converter for {@link BigInteger} values.
 */
public class BigIntegerConverter implements Converter<BigInteger> {

    /**
     * Converts the given value to a {@link BigInteger}.
     * <p>
     * Supports various numeric types and string representation as fall-back.
     *
     * @param value the value to convert, may be {@code null}
     * @return the converted {@link BigInteger}, or {@code null}
     */
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

    /**
     * Returns the target Java class this converter handles.
     *
     * @return {@link BigInteger}.class
     */
    @Override
    public Class<?> type() {
        return BigInteger.class;
    }
}
