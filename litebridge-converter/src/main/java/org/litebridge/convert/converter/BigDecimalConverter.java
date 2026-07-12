package org.litebridge.convert.converter;

import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.sql.Types;

/**
 * A converter for {@link BigDecimal} values.
 * <p>
 * Handles {@link java.sql.Types#NUMERIC} and {@link java.sql.Types#DECIMAL}.
 */
public class BigDecimalConverter implements SqlConverter<BigDecimal> {

    private static final int[] SQL_TYPES = new int[]{Types.NUMERIC, Types.DECIMAL};

    /**
     * Converts the given value to a {@link BigDecimal}.
     * <p>
     * Supports various numeric types and string representation as fall-back.
     *
     * @param value the value to convert, may be {@code null}
     * @return the converted {@link BigDecimal}, or {@code null}
     */
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

    /**
     * Returns the target Java class this converter handles.
     *
     * @return {@link BigDecimal}.class
     */
    @Override
    public Class<?> type() {
        return BigDecimal.class;
    }

    /**
     * Returns the SQL types associated with this converter.
     *
     * @return an array containing {@link java.sql.Types#NUMERIC} and {@link java.sql.Types#DECIMAL}
     */
    @Override
    public int[] sqlTypes() {
        return SQL_TYPES;
    }
}
