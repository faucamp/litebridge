package org.litebridgedb.convert.converter;

import org.jspecify.annotations.Nullable;

/**
 * A functional interface for converting an object to a specific type.
 *
 * @param <T> the target type of the conversion
 */
@FunctionalInterface
public interface ConverterFunction<T> {

    /**
     * Converts the given value to the target type.
     *
     * @param value the value to convert, may be {@code null}
     * @return the converted value, or {@code null} if the input was {@code null} or could not be converted
     */
    @Nullable T convert(final @Nullable Object value);
}
