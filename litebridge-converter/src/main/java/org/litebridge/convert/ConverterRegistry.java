package org.litebridge.convert;

import org.jspecify.annotations.Nullable;
import org.litebridge.convert.converter.Converter;
import org.litebridge.convert.converter.ConverterFunction;
import org.litebridge.convert.converter.GenericConverter;
import org.litebridge.convert.converter.GenericSqlConverter;
import org.litebridge.convert.converter.SqlConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;

final class ConverterRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConverterRegistry.class);
    private final Map<Class<?>, Converter<?>> classConverterMap = new ConcurrentHashMap<>();
    private final Map<Integer, SqlConverter<?>> sqlDataTypeConverterMap = new ConcurrentHashMap<>();

    public void register(final Converter<?> converter) {
        if (classConverterMap.containsKey(converter.type())) {
            LOGGER.warn("Overriding existing converter for type '{}': {}", converter.type(), classConverterMap.get(converter.type()));
        }

        LOGGER.debug("Registering converter for type '{}': {}", converter.type(), converter);
        classConverterMap.put(converter.type(), converter);

        if (converter.primitiveType() != null) {
            if (classConverterMap.containsKey(converter.primitiveType())) {
                LOGGER.warn("Overriding existing converter for primitive type '{}': {}", converter.type(), classConverterMap.get(converter.primitiveType()));
            }

            LOGGER.debug("Registering converter for primitive type '{}': {}", converter.primitiveType(), converter);
            classConverterMap.put(converter.primitiveType(), converter);
        }

        if (converter instanceof SqlConverter<?> sqlConverter) {
            for (final int sqlType : sqlConverter.sqlTypes()) {
                LOGGER.debug("Registering converter for SQL type '{}': {}", sqlType, converter);

                if (sqlDataTypeConverterMap.containsKey(sqlType)) {
                    LOGGER.warn("Overriding existing converter for SQL type '{}': {}", converter.type(), sqlDataTypeConverterMap.get(sqlType));
                }

                sqlDataTypeConverterMap.put(sqlType, sqlConverter);
            }
        }
    }

    public <T> void register(final Class<T> type, final ConverterFunction<T> converterFunction) {
        final Converter<T> converter;

        if (converterFunction instanceof Converter<T> otherConverter) {
            converter = new DelegatingConverter<>(otherConverter);
        } else {
            converter = new GenericConverter<>(type, converterFunction);
        }

        register(converter);
    }

    public <T> void register(final Class<T> type, final int[] sqlTypes, final ConverterFunction<T> converterFunction) {
        final Converter<T> converter;

        if (converterFunction instanceof Converter<T> otherConverter) {
            converter = new DelegatingSqlConverter<>(sqlTypes, otherConverter);
        } else {
            converter = new GenericSqlConverter<>(type, sqlTypes, converterFunction);
        }

        register(converter);
    }

    public void unregister(final Class<?> type) {
        LOGGER.debug("Unregistering converter for type: {}", type);
        final Converter<?> converter = classConverterMap.remove(type);

        if (converter instanceof SqlConverter<?> sqlConverter) {
            for (final int sqlType : sqlConverter.sqlTypes()) {
                LOGGER.debug("Cascade unregistering converter for SQL type: {}", sqlType);
                sqlDataTypeConverterMap.remove(sqlType);
            }
        }
    }

    public void unregister(final int sqlType) {
        LOGGER.debug("Unregistering converter for SQL type: {}", sqlType);
        final Converter<?> converter = sqlDataTypeConverterMap.remove(sqlType);

        if (converter != null) {
            LOGGER.debug("Cascade unregistering converter for type: {}", converter.type());
            classConverterMap.remove(converter.type());
        }
    }

    @SuppressWarnings("unchecked")
    public <T> @Nullable Converter<T> getConverter(final Class<T> type) {
        return (Converter<T>) classConverterMap.get(type);
    }

    @SuppressWarnings("unchecked")
    public <T> @Nullable Converter<T> getConverter(final int sqlType) {
        return (Converter<T>) sqlDataTypeConverterMap.get(sqlType);
    }

    private static class DelegatingConverter<T> implements Converter<T> {

        protected final Converter<T> delegate;

        private DelegatingConverter(final Converter<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public Class<?> type() {
            return delegate.type();
        }

        @Override
        public @Nullable T convert(final @Nullable Object value) {
            return delegate.convert(value);
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", DelegatingConverter.class.getSimpleName() + "[", "]")
                    .add("delegate=" + delegate)
                    .toString();
        }
    }

    private static class DelegatingSqlConverter<T> extends DelegatingConverter<T> implements SqlConverter<T> {

        private final int[] sqlTypes;

        private DelegatingSqlConverter(final int[] sqlTypes, final Converter<T> delegate) {
            super(delegate);
            this.sqlTypes = sqlTypes;
        }

        @Override
        public int[] sqlTypes() {
            return sqlTypes;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", DelegatingSqlConverter.class.getSimpleName() + "[", "]")
                    .add("sqlTypes=" + Arrays.toString(sqlTypes))
                    .add("delegate=" + delegate)
                    .toString();
        }
    }
}
