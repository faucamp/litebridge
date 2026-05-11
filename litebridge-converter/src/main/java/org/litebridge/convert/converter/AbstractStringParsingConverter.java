package org.litebridge.convert.converter;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.StringUtils;

/**
 * An abstract base class for {@link Converter} implementations that primarily convert values by parsing their string representation.
 * <p>
 * This class handles {@code null} values, identity conversions (where the input is already an instance of the target type),
 * and delegates the parsing of non-blank strings to {@link #convertString(String)}.
 *
 * @param <T> the target type
 */
public abstract class AbstractStringParsingConverter<T> implements Converter<T> {

    /**
     * Converts the given value to the target type.
     * <p>
     * The conversion process follows these steps:
     * <ol>
     *     <li>If the value is {@code null}, returns {@code null}.</li>
     *     <li>If the value is already an instance of the target type, returns it as-is.</li>
     *     <li>Otherwise, converts the value to a string and, if not blank, calls {@link #convertString(String)}.</li>
     * </ol>
     *
     * @param value the value to convert, may be {@code null}
     * @return the converted value, or {@code null}
     */
    @Override
    @SuppressWarnings("unchecked")
    public @Nullable T convert(final @Nullable Object value) {
        if (value == null) {
            return null;
        }

        if (type().isAssignableFrom(value.getClass())) {
            return (T) value;
        }

        final String valueStr = value.toString();

        if (StringUtils.isBlank(valueStr)) {
            return null;
        } else {
            return convertString(valueStr);
        }
    }

    /**
     * Parses a string value into the target type.
     *
     * @param value the string value to parse
     * @return the parsed value
     */
    protected abstract T convertString(final String value);
}
