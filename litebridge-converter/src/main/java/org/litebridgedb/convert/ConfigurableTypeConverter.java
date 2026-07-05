package org.litebridgedb.convert;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.commons.ObjectUtils;
import org.litebridgedb.convert.converter.Converter;
import org.litebridgedb.convert.converter.ConverterFunction;
import org.litebridgedb.convert.converter.SqlConverter;
import org.litebridgedb.db.spi.convert.TypeConverter;

/**
 * A concrete implementation of {@link TypeConverter} that allows manual registration and unregistration of converters.
 * <p>
 * This class provides a flexible way to manage {@link Converter} instances for both Java types and SQL types.
 */
public class ConfigurableTypeConverter implements TypeConverter {

    private final ConverterRegistry converterRegistry = new ConverterRegistry();

    /**
     * Converts a value to a database-specific representation (or vice-versa) based on the {@link java.sql.Types} code.
     *
     * @param value      the value to convert, may be {@code null}
     * @param dbDataType the {@link java.sql.Types} code for the database data type
     * @return the converted value, or {@code null} if the input was {@code null}
     * @throws IllegalArgumentException if no converter is found for the specified SQL type
     */
    @Override
    public @Nullable Object convert(@Nullable final Object value, final int dbDataType) {
        final Converter<?> converter = converterRegistry.getConverter(dbDataType);

        if (converter == null) {
            throw new IllegalArgumentException("No converter found for SQL type: " + dbDataType);
        }

        return converter.convert(value);
    }

    /**
     * Converts a value to a specific Java type.
     *
     * @param value the value to convert, may be {@code null}
     * @param type  the target Java type
     * @param <T>   the target Java type
     * @return the converted value, or {@code null} if the input was {@code null}
     * @throws IllegalArgumentException if no converter is found for the specified Java type
     */
    @Override
    public @Nullable <T> T convert(@Nullable final Object value, final Class<T> type) {
        final Converter<T> converter = converterRegistry.getConverter(type);

        if (converter == null) {
            if (value != null && type.isAssignableFrom(value.getClass())) {
                // No conversion needed
                return type.cast(value);
            }

            throw new IllegalArgumentException("No converter found for class: " + type);
        }

        return converter.convert(value);
    }

    @Override
    public int getSqlDataType(final Class<?> fieldType) throws IllegalArgumentException {
        final Converter<?> converter = ObjectUtils.requireNonNull(converterRegistry.getConverter(fieldType),
                () -> new IllegalArgumentException("No converter found for class: " + fieldType.getName()));

        if (converter instanceof SqlConverter<?> sqlConverter) {
            return sqlConverter.sqlTypes()[0];
        } else {
            throw new IllegalArgumentException("No SQL type converter found for class: " + fieldType.getName());
        }
    }

    @Override
    public Class<?> getClassForSqlType(final int sqlDataType) throws IllegalArgumentException {
        final Converter<?> converter = ObjectUtils.requireNonNull(converterRegistry.getConverter(sqlDataType),
                () -> new IllegalArgumentException("No converter found for SQL type: " + sqlDataType));

        return converter.type();
    }

    /**
     * Registers a new converter.
     *
     * @param converter the converter to register
     */
    public void register(final Converter<?> converter) {
        converterRegistry.register(converter);
    }

    /**
     * Registers a converter for a specific Java type using a functional interface.
     *
     * @param type              the target Java type
     * @param converterFunction the conversion logic
     * @param <T>               the target Java type
     */
    public <T> void register(final Class<T> type, final ConverterFunction<T> converterFunction) {
        converterRegistry.register(type, converterFunction);
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
        converterRegistry.register(type, sqlTypes, converterFunction);
    }

    /**
     * Removes a converter for a specific Java type.
     *
     * @param type the Java type to unregister
     */
    public void unregister(final Class<?> type) {
        converterRegistry.unregister(type);
    }

    /**
     * Removes a converter for a specific SQL type.
     *
     * @param sqlType the {@link java.sql.Types} code to unregister
     */
    public void unregister(final int sqlType) {
        converterRegistry.unregister(sqlType);
    }
}
