package org.litebridgedb.convert;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.litebridgedb.convert.converter.Converter;
import org.litebridgedb.convert.converter.ConverterFunction;
import org.litebridgedb.convert.converter.SqlConverter;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConverterRegistryTest {

    @Test
    void register_andGetConverter_javaType() {
        // Given
        ConverterRegistry registry = new ConverterRegistry();
        Converter<String> converter = new TestConverter<>(String.class);

        // When
        registry.register(converter);

        // Then
        assertEquals(converter, registry.getConverter(String.class));
    }

    @Test
    void register_andGetConverter_sqlType() {
        // Given
        ConverterRegistry registry = new ConverterRegistry();
        SqlConverter<String> converter = new TestSqlConverter<>(String.class, new int[]{1, 2});

        // When
        registry.register(converter);

        // Then
        assertEquals(converter, registry.getConverter(String.class));
        assertEquals(converter, registry.getConverter(1));
        assertEquals(converter, registry.getConverter(2));
    }

    @Test
    void testRegister_overrideWarning() {
        // Given
        ConverterRegistry registry = new ConverterRegistry();
        Converter<String> converter1 = new TestConverter<>(String.class);
        Converter<String> converter2 = new TestConverter<>(String.class);

        // When
        registry.register(converter1);
        registry.register(converter2); // Should log warning

        // Then
        assertEquals(converter2, registry.getConverter(String.class));
    }

    @Test
    void testRegisterSql_overrideWarning() {
        // Given
        ConverterRegistry registry = new ConverterRegistry();
        SqlConverter<String> converter1 = new TestSqlConverter<>(String.class, new int[]{1});
        SqlConverter<Integer> converter2 = new TestSqlConverter<>(Integer.class, new int[]{1});

        // When
        registry.register(converter1);
        registry.register(converter2); // Should log warning for SQL type 1

        // Then
        assertEquals(converter1, registry.getConverter(String.class));
        assertEquals(converter2, registry.getConverter(Integer.class));
        assertEquals(converter2, registry.getConverter(1));
    }

    @Test
    void testRegister_converterFunction() {
        // Given
        ConverterRegistry registry = new ConverterRegistry();
        ConverterFunction<String> function = value -> value == null ? null : value.toString();

        // When
        registry.register(String.class, function);

        // Then
        Converter<String> converter = registry.getConverter(String.class);
        assertNotNull(converter);
        assertEquals(String.class, converter.type());
        assertEquals("test", converter.convert("test"));
    }

    @Test
    void testRegister_converterFunctionAsConverter() {
        // Given
        ConverterRegistry registry = new ConverterRegistry();
        Converter<String> existingConverter = new TestConverter<>(String.class);

        // When
        registry.register(String.class, (ConverterFunction<String>) existingConverter);

        // Then
        Converter<String> converter = registry.getConverter(String.class);
        assertNotNull(converter);
        // Should be a DelegatingConverter
        assertTrue(converter.toString().contains("DelegatingConverter"));
        assertEquals("test", converter.convert("test"));
    }

    @Test
    void testRegisterSql_converterFunction() {
        // Given
        ConverterRegistry registry = new ConverterRegistry();
        ConverterFunction<String> function = value -> value == null ? null : value.toString();
        int[] sqlTypes = {1, 2};

        // When
        registry.register(String.class, sqlTypes, function);

        // Then
        Converter<String> converter = registry.getConverter(String.class);
        assertNotNull(converter);
        assertEquals(String.class, converter.type());
        assertEquals(converter, registry.getConverter(1));
        assertEquals(converter, registry.getConverter(2));
    }

    @Test
    void testRegisterSql_converterFunctionAsConverter() {
        // Given
        ConverterRegistry registry = new ConverterRegistry();
        Converter<String> existingConverter = new TestConverter<>(String.class);
        int[] sqlTypes = {1};

        // When
        registry.register(String.class, sqlTypes, (ConverterFunction<String>) existingConverter);

        // Then
        Converter<String> converter = registry.getConverter(String.class);
        assertNotNull(converter);
        assertTrue(converter.toString().contains("DelegatingSqlConverter"));
        assertEquals(converter, registry.getConverter(1));
    }

    @Test
    void unregister_javaType() {
        // Given
        ConverterRegistry registry = new ConverterRegistry();
        Converter<String> converter = new TestConverter<>(String.class);
        registry.register(converter);

        // When
        registry.unregister(String.class);

        // Then
        assertNull(registry.getConverter(String.class));
    }

    @Test
    void unregister_javaTypeWithSqlCascade() {
        // Given
        ConverterRegistry registry = new ConverterRegistry();
        SqlConverter<String> converter = new TestSqlConverter<>(String.class, new int[]{1});
        registry.register(converter);

        // When
        registry.unregister(String.class);

        // Then
        assertNull(registry.getConverter(String.class));
        assertNull(registry.getConverter(1));
    }

    @Test
    void unregister_sqlType() {
        // Given
        ConverterRegistry registry = new ConverterRegistry();
        SqlConverter<String> converter = new TestSqlConverter<>(String.class, new int[]{1});
        registry.register(converter);

        // When
        registry.unregister(1);

        // Then
        assertNull(registry.getConverter(1));
        assertNull(registry.getConverter(String.class)); // Should cascade
    }

    @Test
    void unregister_nonExistentSqlType() {
        // Given
        ConverterRegistry registry = new ConverterRegistry();

        // When/Then (should not throw)
        registry.unregister(1);
    }

    @Test
    void testDelegatingConverter_toString() {
        Converter<String> delegate = new TestConverter<>(String.class);
        registryRegisterDelegating(delegate);
    }

    // Helper to access package private inner classes if needed or just trigger their code
    private void registryRegisterDelegating(Converter<String> delegate) {
        ConverterRegistry registry = new ConverterRegistry();
        registry.register(String.class, (ConverterFunction<String>) delegate);
        Converter<String> converter = registry.getConverter(String.class);
        assertNotNull(converter);
        String toString = converter.toString();
        assertTrue(toString.contains("DelegatingConverter"));
        assertTrue(toString.contains("delegate=" + delegate));
    }

    @Test
    void testDelegatingSqlConverter_ToString() {
        Converter<String> delegate = new TestConverter<>(String.class);
        int[] sqlTypes = {1};
        ConverterRegistry registry = new ConverterRegistry();
        registry.register(String.class, sqlTypes, (ConverterFunction<String>) delegate);
        Converter<String> converter = registry.getConverter(String.class);
        assertNotNull(converter);
        String toString = converter.toString();
        assertTrue(toString.contains("DelegatingSqlConverter"));
        assertTrue(toString.contains("sqlTypes=[1]"));
        assertTrue(toString.contains("delegate=" + delegate));

        SqlConverter<String> sqlConverter = (SqlConverter<String>) converter;
        assertArrayEquals(sqlTypes, sqlConverter.sqlTypes());
    }

    @Test
    void testRegister_PrimitiveType() {
        // Given
        ConverterRegistry registry = new ConverterRegistry();
        Converter<Integer> converter = new TestConverter<>(Integer.class, int.class);

        // When
        registry.register(converter);

        // Then
        assertEquals(converter, registry.getConverter(Integer.class));
        assertEquals(converter, registry.getConverter(int.class));
    }

    @Test
    void testRegister_PrimitiveOverrideWarning() {
        // Given
        ConverterRegistry registry = new ConverterRegistry();
        Converter<Integer> converter1 = new TestConverter<>(Integer.class, int.class);
        Converter<Integer> converter2 = new TestConverter<>(Integer.class, int.class);

        // When
        registry.register(converter1);
        registry.register(converter2); // Should trigger warnings for both Integer.class and int.class

        // Then
        assertEquals(converter2, registry.getConverter(Integer.class));
        assertEquals(converter2, registry.getConverter(int.class));
    }

    @Test
    void testDelegatingConverter_PrimitiveType() {
        // Given
        Converter<Integer> delegate = new TestConverter<>(Integer.class, int.class);
        ConverterRegistry registry = new ConverterRegistry();
        registry.register(Integer.class, (ConverterFunction<Integer>) delegate);

        // When
        Converter<Integer> converter = registry.getConverter(Integer.class);

        // Then
        assertNotNull(converter);
        assertEquals(int.class, converter.primitiveType());
    }

    private static class TestConverter<T> implements Converter<T> {
        private final Class<T> type;
        private final Class<?> primitiveType;

        TestConverter(Class<T> type) {
            this(type, null);
        }

        TestConverter(Class<T> type, Class<?> primitiveType) {
            this.type = type;
            this.primitiveType = primitiveType;
        }

        @Override
        public Class<?> type() {
            return type;
        }

        @Override
        public @Nullable Class<?> primitiveType() {
            return primitiveType;
        }

        @Override
        public @Nullable T convert(@Nullable Object value) {
            return (T) value;
        }
    }

    private static class TestSqlConverter<T> extends TestConverter<T> implements SqlConverter<T> {
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
