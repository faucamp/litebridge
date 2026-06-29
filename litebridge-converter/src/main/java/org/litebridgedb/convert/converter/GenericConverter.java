package org.litebridgedb.convert.converter;

import org.jspecify.annotations.Nullable;

/**
 * A generic implementation of {@link Converter} that uses a {@link ConverterFunction} for the conversion logic.
 *
 * @param <T> the target type
 */
public class GenericConverter<T> implements Converter<T> {

    private final Class<T> type;
    private final ConverterFunction<T> conversionFunction;

    /**
     * Constructs a new {@code GenericConverter} for the specified type and conversion function.
     *
     * @param type the target Java class
     * @param conversionFunction the conversion logic
     */
    public GenericConverter(final Class<T> type, final ConverterFunction<T> conversionFunction) {
        this.type = type;
        this.conversionFunction = conversionFunction;
    }

    /**
     * Returns the target Java class this converter handles.
     *
     * @return the target Java class
     */
    @Override
    public Class<T> type() {
        return type;
    }

    /**
     * Converts the given value to the target type using the provided conversion function.
     *
     * @param value the value to convert, may be {@code null}
     * @return the converted value, or {@code null}
     */
    @Override
    public @Nullable T convert(final @Nullable Object value) {
        return conversionFunction.convert(value);
    }
}
