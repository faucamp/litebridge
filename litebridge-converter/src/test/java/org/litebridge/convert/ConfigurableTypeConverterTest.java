package org.litebridge.convert;

import org.junit.jupiter.api.Test;
import org.litebridge.convert.converter.Converter;
import org.jspecify.annotations.Nullable;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurableTypeConverterTest {

    @Test
    void testRegisterAndConvertBySqlType() {
        // Given
        ConfigurableTypeConverter typeConverter = new ConfigurableTypeConverter();
        typeConverter.register(new TestSqlConverter<>(String.class, new int[]{1}));

        // When
        Object result = typeConverter.convert("test", 1);

        // Then
        assertEquals("test", result);
    }

    @Test
    void testConvertBySqlType_NotFound() {
        // Given
        ConfigurableTypeConverter typeConverter = new ConfigurableTypeConverter();

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> typeConverter.convert("test", 1));
    }

    @Test
    void testRegisterAndConvertByJavaType() {
        // Given
        ConfigurableTypeConverter typeConverter = new ConfigurableTypeConverter();
        typeConverter.register(new TestConverter<>(String.class));

        // When
        String result = typeConverter.convert("test", String.class);

        // Then
        assertEquals("test", result);
    }

    @Test
    void testConvertByJavaType_NotFound() {
        // Given
        ConfigurableTypeConverter typeConverter = new ConfigurableTypeConverter();

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> typeConverter.convert("test", String.class));
    }

    @Test
    void testRegisterWithFunction() {
        // Given
        ConfigurableTypeConverter typeConverter = new ConfigurableTypeConverter();
        typeConverter.register(String.class, new int[]{1}, value -> value == null ? null : value.toString());

        // When
        Object result = typeConverter.convert("test", 1);
        String result2 = typeConverter.convert("test", String.class);

        // Then
        assertEquals("test", result);
        assertEquals("test", result2);
    }

    @Test
    void testUnregisterByJavaType() {
        // Given
        ConfigurableTypeConverter typeConverter = new ConfigurableTypeConverter();
        typeConverter.register(new TestConverter<>(String.class));
        assertNotNull(typeConverter.convert("test", String.class));

        // When
        typeConverter.unregister(String.class);

        // Then
        assertThrows(IllegalArgumentException.class, () -> typeConverter.convert("test", String.class));
    }

    @Test
    void testUnregisterBySqlType() {
        // Given
        ConfigurableTypeConverter typeConverter = new ConfigurableTypeConverter();
        typeConverter.register(new TestSqlConverter<>(String.class, new int[]{1}));
        assertNotNull(typeConverter.convert("test", 1));

        // When
        typeConverter.unregister(1);

        // Then
        assertThrows(IllegalArgumentException.class, () -> typeConverter.convert("test", 1));
    }

    private static class TestConverter<T> implements Converter<T> {
        private final Class<T> type;
        TestConverter(Class<T> type) { this.type = type; }
        @Override public Class<?> type() { return type; }
        @Override public @Nullable T convert(@Nullable Object value) { return (T) value; }
    }

    private static class TestSqlConverter<T> extends TestConverter<T> implements org.litebridge.convert.converter.SqlConverter<T> {
        private final int[] sqlTypes;
        TestSqlConverter(Class<T> type, int[] sqlTypes) { super(type); this.sqlTypes = sqlTypes; }
        @Override public int[] sqlTypes() { return sqlTypes; }
    }
}
