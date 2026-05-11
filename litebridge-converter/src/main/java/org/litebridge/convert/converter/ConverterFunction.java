package org.litebridge.convert.converter;

import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface ConverterFunction<T> {

    @Nullable T convert(final @Nullable Object value);
}
