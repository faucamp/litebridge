package org.litebridge.convert;

import org.jspecify.annotations.Nullable;
import org.litebridge.convert.converter.Converter;
import org.litebridge.convert.converter.ConverterFunction;
import org.litebridge.convert.converter.GenericConverter;
import org.litebridge.convert.converter.GenericSqlConverter;
import org.litebridge.convert.converter.SqlConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.JDBCType;
import java.util.Arrays;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An internal registry for managing {@link Converter} instances.
 * <p>
 * This class provides thread-safe storage and retrieval of converters based on Java types and SQL types.
 */
final class ConverterRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConverterRegistry.class);
    private final Map<Class<?>, Converter<?>> classConverterMap = new ConcurrentHashMap<>();
    private final Map<Integer, SqlConverter<?>> sqlDataTypeConverterMap = new ConcurrentHashMap<>();

    /**
     * Registers a converter for the type it handles.
     * <p>
     * If the converter is a {@link SqlConverter}, it is also registered for its associated SQL types.
     *
     * @param converter the converter to register
     */
    public void register(final Converter<?> converter) {
        registerConverter(converter);
        registerPrimitiveConverter(converter);
        registerSqlConverter(converter);
    }

    private void registerConverter(final Converter<?> converter) {
        final Converter<?> existingConverter = classConverterMap.get(converter.type());

        if (existingConverter != null) {
            if (existingConverter.priority() < converter.priority()) {
                LOGGER.trace("Not overriding converter for {} from: {} to: {}; existing has priority", converter.type(), existingConverter.getClass().getSimpleName(), converter.getClass().getSimpleName());
                return;
            }

            LOGGER.debug("Overriding converter for {} from: {} to: {}", converter.type(), existingConverter.getClass().getSimpleName(), converter.getClass().getSimpleName());
        }

        LOGGER.trace("Registering converter for type '{}': {}", converter.type(), converter);
        classConverterMap.put(converter.type(), converter);
    }

    private void registerPrimitiveConverter(final Converter<?> converter) {
        final Class<?> primitiveType = converter.primitiveType();

        if (primitiveType == null) {
            return;
        }

        final Converter<?> existingConverter = classConverterMap.get(primitiveType);

        if (existingConverter != null) {
            if (existingConverter.priority() < converter.priority()) {
                LOGGER.trace("Not overriding converter for primitive type {} from: {} to: {}; existing has priority", primitiveType, existingConverter.getClass().getSimpleName(), converter.getClass().getSimpleName());
                return;
            }

            LOGGER.debug("Overriding converter for primitive type {} from: {} to: {}", primitiveType, existingConverter.getClass().getSimpleName(), converter.getClass().getSimpleName());
        }

        LOGGER.trace("Registering converter for primitive type '{}': {}", primitiveType, converter);
        classConverterMap.put(primitiveType, converter);
    }

    private void registerSqlConverter(final Converter<?> converter) {
        if (!(converter instanceof SqlConverter<?> sqlConverter)) {
            return;
        }

        for (final int sqlType : sqlConverter.sqlTypes()) {

            final Converter<?> existingConverter = sqlDataTypeConverterMap.get(sqlType);

            if (existingConverter != null) {
                if (existingConverter.priority() < converter.priority()) {
                    LOGGER.trace("Not overriding SQL converter for SQL type {} / {} from {} to {}; existing has priority", JDBCType.valueOf(sqlType), converter.type(), sqlDataTypeConverterMap.get(sqlType).getClass().getSimpleName(), converter.getClass().getSimpleName());
                    return;
                }

                LOGGER.debug("Overriding SQL converter for SQL type {} / {} from {} to {}", JDBCType.valueOf(sqlType), converter.type(), sqlDataTypeConverterMap.get(sqlType).getClass().getSimpleName(), converter.getClass().getSimpleName());
            }

            LOGGER.trace("Registering converter for SQL type '{}': {}", sqlType, converter);
            sqlDataTypeConverterMap.put(sqlType, sqlConverter);
        }
    }

    /**
     * Registers a converter for a specific Java type using a functional interface.
     *
     * @param type              the target Java type
     * @param converterFunction the conversion logic
     * @param <T>               the target Java type
     */
    public <T> void register(final Class<T> type, final ConverterFunction<T> converterFunction) {
        final Converter<T> converter;

        if (converterFunction instanceof Converter<T> otherConverter) {
            converter = new DelegatingConverter<>(otherConverter);
        } else {
            converter = new GenericConverter<>(type, converterFunction);
        }

        register(converter);
    }

    /**
     * Registers a converter for a specific Java type and its associated SQL types using a functional interface.
     *
     * @param type              the target Java type
     * @param sqlTypes          an array of {@link java.sql.Types} codes associated with this converter
     * @param converterFunction the conversion logic
     * @param <T>               the target Java type
     */
    public <T> void register(final Class<T> type, final int[] sqlTypes, final ConverterFunction<T> converterFunction) {
        final Converter<T> converter;

        if (converterFunction instanceof Converter<T> otherConverter) {
            converter = new DelegatingSqlConverter<>(sqlTypes, otherConverter);
        } else {
            converter = new GenericSqlConverter<>(type, sqlTypes, converterFunction);
        }

        register(converter);
    }

    /**
     * Removes a converter for a specific Java type.
     *
     * @param type the Java type to unregister
     */
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

    /**
     * Removes a converter for a specific SQL type.
     *
     * @param sqlType the {@link java.sql.Types} code to unregister
     */
    public void unregister(final int sqlType) {
        LOGGER.debug("Unregistering converter for SQL type: {}", sqlType);
        final Converter<?> converter = sqlDataTypeConverterMap.remove(sqlType);

        if (converter != null) {
            LOGGER.debug("Cascade unregistering converter for type: {}", converter.type());
            classConverterMap.remove(converter.type());
        }
    }

    /**
     * Returns a converter for the specified Java type.
     *
     * @param type the target Java type
     * @param <T>  the target Java type
     * @return the converter, or {@code null} if none is found
     */
    @SuppressWarnings("unchecked")
    public <T> @Nullable Converter<T> getConverter(final Class<T> type) {
        return (Converter<T>) classConverterMap.get(type);
    }

    /**
     * Returns a converter for the specified SQL type.
     *
     * @param sqlType the {@link java.sql.Types} code
     * @param <T>     the target Java type
     * @return the converter, or {@code null} if none is found
     */
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
        public @Nullable Class<?> primitiveType() {
            return delegate.primitiveType();
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
