package org.litebridge.convert.converter;

public interface Converter<T> extends ConverterFunction<T> {

    Class<?> type();
}
