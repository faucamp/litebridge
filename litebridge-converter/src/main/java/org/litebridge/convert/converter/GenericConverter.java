package org.litebridge.convert.converter;

import org.jspecify.annotations.Nullable;

public class GenericConverter<T> implements Converter<T> {

    private final Class<T> type;
    private final ConverterFunction<T> conversionFunction;

    public GenericConverter(final Class<T> type, final ConverterFunction<T> conversionFunction) {
        this.type = type;
        this.conversionFunction = conversionFunction;
    }

    @Override
    public Class<T> type() {
        return type;
    }

    @Override
    public @Nullable T convert(final @Nullable Object value) {
        return conversionFunction.convert(value);
    }

}
