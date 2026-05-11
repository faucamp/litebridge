package org.litebridge.convert;

import org.jspecify.annotations.Nullable;
import org.litebridge.convert.converter.Converter;
import org.litebridge.convert.converter.ConverterFunction;
import org.litebridge.db.spi.convert.TypeConverter;

public class ConfigurableTypeConverter implements TypeConverter {

    private final ConverterRegistry converterRegistry = new ConverterRegistry();

    @Override
    public @Nullable Object convert(@Nullable final Object value, final int dbDataType) {
        final Converter<?> converter = converterRegistry.getConverter(dbDataType);

        if (converter == null) {
            throw new IllegalArgumentException("No converter found for SQL type: " + dbDataType);
        }

        return converter.convert(value);
    }

    @Override
    public @Nullable <T> T convert(@Nullable final Object value, final Class<T> type) {
        final Converter<T> converter = converterRegistry.getConverter(type);

        if (converter == null) {
            throw new IllegalArgumentException("No converter found for class: " + type);
        }

        return converter.convert(value);
    }

    public void register(final Converter<?> converter) {
        converterRegistry.register(converter);
    }

    public <T> void register(final Class<T> type, final int[] sqlTypes, final ConverterFunction<T> converterFunction) {
        converterRegistry.register(type, sqlTypes, converterFunction);
    }

    public void unregister(final Class<?> type) {
        converterRegistry.unregister(type);
    }

    public void unregister(final int sqlType) {
        converterRegistry.unregister(sqlType);
    }
}
