package org.litebridge.convert.converter;

public interface SqlConverter<T> extends Converter<T> {

    int[] sqlTypes();
}
