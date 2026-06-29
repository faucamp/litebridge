package org.litebridgedb.convert.converter;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.commons.StringUtils;

/**
 * An abstract base class for {@link Converter} implementations that handle numeric types.
 * <p>
 * This class provides common logic for converting values to a specific {@link Number} subclass,
 * including handling {@code null} values, identity conversions, numeric-to-numeric conversions,
 * and string parsing.
 *
 * @param <T> the target numeric type
 */
public abstract class AbstractNumberConverter<T extends Number> implements Converter<T> {

    /**
     * Converts the given value to the target numeric type.
     * <p>
     * The conversion process follows these steps:
     * <ol>
     *     <li>If the value is {@code null}, returns {@code null}.</li>
     *     <li>If the value is already of the target type, returns it as-is.</li>
     *     <li>If the value is a {@link Number}, calls {@link #convertNumber(Number)}.</li>
     *     <li>Otherwise, converts the value to a string and, if not blank, calls {@link #convertString(String)}.</li>
     * </ol>
     *
     * @param value the value to convert, may be {@code null}
     * @return the converted numeric value, or {@code null}
     */
    @Override
    @SuppressWarnings("unchecked")
    public @Nullable T convert(final @Nullable Object value) {
        if (value == null) {
            return null;
        }

        if (type() == value.getClass()) {
            return (T) value;
        } else if (value instanceof Number number) {
            return convertNumber(number);
        }

        final String valueStr = value.toString();

        if (StringUtils.isBlank(valueStr)) {
            return null;
        } else {
            return convertString(valueStr);
        }
    }

    /**
     * Converts a {@link Number} instance to the target type.
     *
     * @param value the numeric value to convert
     * @return the converted value
     */
    protected abstract T convertNumber(final Number value);

    /**
     * Parses a string value into the target type.
     *
     * @param value the string value to parse
     * @return the parsed value
     */
    protected abstract T convertString(final String value);
}
