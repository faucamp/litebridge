package org.litebridgedb.convert;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.litebridgedb.convert.converter.Converter;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfigurableTypeConverterTest {

    @Test
    void register_convertByJavaType() {
        // Given
        final ConfigurableTypeConverter typeConverter = new ConfigurableTypeConverter();
        typeConverter.register(new TestConverter<>(String.class));

        // When
        String result = typeConverter.convert("test", String.class);

        // Then
        assertEquals("test", result);
    }

    @Test
    void register_convertBySqlType() {
        // Given
        final ConfigurableTypeConverter typeConverter = new ConfigurableTypeConverter();
        typeConverter.register(new TestSqlConverter<>(String.class, new int[]{1}));

        // When
        Object result = typeConverter.convert("test", 1);

        // Then
        assertEquals("test", result);
    }

    @Test
    void register_multipleConverters() {
        // Given
        final ConfigurableTypeConverter typeConverter = new ConfigurableTypeConverter();
        typeConverter.register(new TestConverter<>(String.class));
        typeConverter.register(new TestConverter<>(Integer.class));

        // When
        String stringResult = typeConverter.convert("testString", String.class);
        Integer intResult = typeConverter.convert(123, Integer.class);

        // Then
        assertEquals("testString", stringResult);
        assertEquals(123, intResult);
    }

    @Test
    void register_multipleSqlConverters() {
        // Given
        final ConfigurableTypeConverter typeConverter = new ConfigurableTypeConverter();
        typeConverter.register(new TestSqlConverter<>(String.class, new int[]{1}));
        typeConverter.register(new TestSqlConverter<>(Byte.class, new int[]{2}));

        // When
        Object stringResult = typeConverter.convert("testSqlString", 1);
        Object byteResult = typeConverter.convert((byte) 42, 2);

        // Then
        assertEquals("testSqlString", stringResult);
        assertEquals((byte) 42, byteResult);
    }

    @Test
    void register_customFunction() {
        // Given
        final ConfigurableTypeConverter typeConverter = new ConfigurableTypeConverter();
        typeConverter.register(Long.class, value -> value == null ? 0L : Long.parseLong(value.toString()));

        // When
        Long result = typeConverter.convert("1000", Long.class);

        // Then
        assertEquals(1000L, result);
    }

    @Test
    void convert_bySqlType_notFound() {
        // Given
        final ConfigurableTypeConverter typeConverter = new ConfigurableTypeConverter();

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> typeConverter.convert("test", 1));
    }

    @Test
    void convert_byJavaType_notFound() {
        // Given
        final ConfigurableTypeConverter typeConverter = new ConfigurableTypeConverter();

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> typeConverter.convert("test", Map.class));
    }

    @Test
    void convert_byJavaType_nullValueAndConverterNotFound() {
        // Given
        final ConfigurableTypeConverter typeConverter = new ConfigurableTypeConverter();

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> typeConverter.convert(null, Object.class));
    }
    
    @Test
    void convert_byJavaType_Object() {
        // Given
        final ConfigurableTypeConverter typeConverter = new ConfigurableTypeConverter();

        // When
        final Object result = typeConverter.convert("test", Object.class);

        // Then
        assertEquals("test", result);
    }

    @Test
    void register_withFunction() {
        // Given
        final ConfigurableTypeConverter typeConverter = new ConfigurableTypeConverter();
        typeConverter.register(String.class, new int[]{1}, value -> value == null ? null : value.toString());

        // When
        Object result = typeConverter.convert("test", 1);
        String result2 = typeConverter.convert("test", String.class);

        // Then
        assertEquals("test", result);
        assertEquals("test", result2);
    }

    @Test
    void getDbDataType_withSqlConverter() {
        // Given
        final ConfigurableTypeConverter typeConverter = new ConfigurableTypeConverter();
        typeConverter.register(new TestSqlConverter<>(String.class, new int[]{1, 2}));

        // When
        final int result = typeConverter.getDbDataType(String.class);

        // Then
        assertEquals(1, result);
    }

    @Test
    void getDbDataType_converterNotFound() {
        // Given
        final ConfigurableTypeConverter typeConverter = new ConfigurableTypeConverter();

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> typeConverter.getDbDataType(String.class));
    }

    @Test
    void getDbDataType_converterIsNotSqlConverter() {
        // Given
        final ConfigurableTypeConverter typeConverter = new ConfigurableTypeConverter();
        typeConverter.register(new TestConverter<>(String.class));

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> typeConverter.getDbDataType(String.class));
    }

    @Test
    void unregister_byJavaType() {
        // Given
        final ConfigurableTypeConverter typeConverter = new ConfigurableTypeConverter();
        typeConverter.register(new TestConverter<>(String.class));
        assertNotNull(typeConverter.convert(123, String.class));

        // When
        typeConverter.unregister(String.class);

        // Then
        assertThrows(IllegalArgumentException.class, () -> typeConverter.convert(123, String.class));
    }

    @Test
    void unregister_bySqlType() {
        // Given
        final ConfigurableTypeConverter typeConverter = new ConfigurableTypeConverter();
        typeConverter.register(new TestSqlConverter<>(String.class, new int[]{1}));
        assertNotNull(typeConverter.convert("test", 1));

        // When
        typeConverter.unregister(1);

        // Then
        assertThrows(IllegalArgumentException.class, () -> typeConverter.convert("test", 1));
    }

    private static class TestConverter<T> implements Converter<T> {
        private final Class<T> type;

        TestConverter(Class<T> type) {
            this.type = type;
        }

        @Override
        public Class<?> type() {
            return type;
        }

        @Override
        public @Nullable T convert(@Nullable Object value) {
            return (T) value;
        }
    }

    private static class TestSqlConverter<T> extends TestConverter<T> implements org.litebridgedb.convert.converter.SqlConverter<T> {
        private final int[] sqlTypes;

        TestSqlConverter(Class<T> type, int[] sqlTypes) {
            super(type);
            this.sqlTypes = sqlTypes;
        }

        @Override
        public int[] sqlTypes() {
            return sqlTypes;
        }
    }
}
