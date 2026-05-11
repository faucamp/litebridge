package org.litebridge.convert.converter;

import org.jspecify.annotations.Nullable;

public interface Converter<T> extends ConverterFunction<T> {

    Class<?> type();

    default @Nullable Class<?> primitiveType() {
        return null;
    }
}
