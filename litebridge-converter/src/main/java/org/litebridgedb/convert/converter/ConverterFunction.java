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
     * Converts the given rhs to the target type.
     *
     * @param value the rhs to convert, may be {@code null}
     * @return the converted rhs, or {@code null} if the input was {@code null} or could not be converted
     */
    @Nullable T convert(final @Nullable Object value);
}
