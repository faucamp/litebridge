package org.litebridge.commons;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

class ClassUtilsTest {

    @Test
    void getAllFields() {
        // When
        final Set<Field> result = ClassUtils.getAllFields(TestDto.class);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(field -> field.getName().equals("name")));
        assertTrue(result.stream().anyMatch(field -> field.getName().equals("age")));
    }

    @Test
    void getAllFields_includeStatic() {
        // When
        final Set<Field> result = ClassUtils.getAllFields(TestDto.class, true);

        // Then
        assertEquals(3, result.size());
        assertTrue(result.stream().anyMatch(field -> field.getName().equals("staticField")));
        assertTrue(result.stream().anyMatch(field -> field.getName().equals("name")));
        assertTrue(result.stream().anyMatch(field -> field.getName().equals("age")));
    }

    @Test
    void getAllFields_inheritance() {
        // When
        final Set<Field> result = ClassUtils.getAllFields(ChildTestDto.class);

        // Then
        assertEquals(3, result.size());
        assertTrue(result.stream().anyMatch(field -> field.getName().equals("active")));
        assertTrue(result.stream().anyMatch(field -> field.getName().equals("name")));
        assertTrue(result.stream().anyMatch(field -> field.getName().equals("age")));
    }

    @Test
    void getField() {
        // Given
        final String fieldName = "age";

        // When
        final Field field = ClassUtils.getField(TestDto.class, fieldName);

        // Then
        assertNotNull(field);
        assertEquals(fieldName, field.getName());
    }

    @Test
    void getField_notFound() {
        // Given
        final String fieldName = "nonExistingField";

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> ClassUtils.getField(TestDto.class, fieldName));
    }

    @Test
    void getField_inheritedField() {
        // Given
        final String fieldName = "age";

        // When
        final Field field = ClassUtils.getField(ChildTestDto.class, fieldName);

        // Then
        assertNotNull(field);
        assertEquals(fieldName, field.getName());
    }

    @Test
    void getField_inheritance_notFound() {
        // Given
        final String fieldName = "nonExistingField";

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> ClassUtils.getField(ChildTestDto.class, fieldName));
    }

    @Test
    void isBasicType_String() {
        // Given
        final String object = "Hello World!";

        // When
        final boolean result = ClassUtils.isBasicType(object.getClass());

        // Then
        assertTrue(result);
    }

    @Test
    void isBasicType_Short() {
        // Given
        final Short object = Short.valueOf((short) 1);

        // When
        final boolean result = ClassUtils.isBasicType(object.getClass());

        // Then
        assertTrue(result);
    }

    @Test
    void isBasicType_Integer() {
        // Given
        final Integer object = Integer.valueOf(1);

        // When
        final boolean result = ClassUtils.isBasicType(object.getClass());

        // Then
        assertTrue(result);
    }

    @Test
    void isBasicType_int() {
        // Given
        final Field ageField = ClassUtils.getField(TestDto.class, "age");

        // When
        final boolean result = ClassUtils.isBasicType(ageField.getType());

        // Then
        assertTrue(result);
    }

    @Test
    void isBasicType_Long() {
        // Given
        final Long object = Long.valueOf(1);

        // When
        final boolean result = ClassUtils.isBasicType(object.getClass());

        // Then
        assertTrue(result);
    }

    @Test
    void isBasicType_BigDecimal() {
        // Given
        final BigDecimal object = BigDecimal.valueOf(1);

        // When
        final boolean result = ClassUtils.isBasicType(object.getClass());

        // Then
        assertTrue(result);
    }

    @Test
    void isBasicType_Double() {
        // Given
        final Double object = Double.valueOf(1.234);

        // When
        final boolean result = ClassUtils.isBasicType(object.getClass());

        // Then
        assertTrue(result);
    }

    @Test
    void isBasicType_Float() {
        // Given
        final Float object = Float.valueOf(1.234f);

        // When
        final boolean result = ClassUtils.isBasicType(object.getClass());

        // Then
        assertTrue(result);
    }

    @Test
    void isBasicType_Enum() {
        // When
        final boolean result = ClassUtils.isBasicType(TestEnum.class);

        // Then
        assertTrue(result);
    }

    @Test
    void isBasicType_Boolean() {
        // Given
        final Boolean object = Boolean.TRUE;

        // When
        final boolean result = ClassUtils.isBasicType(object.getClass());

        // Then
        assertTrue(result);
    }

    @Test
    void isBasicType_byteArray() {
        // Given
        final byte[] byteArray = new byte[1];

        // When
        final boolean result = ClassUtils.isBasicType(byteArray.getClass());

        // Then
        assertTrue(result);
    }

    @Test
    void getGenericType() {
        // Given
        final Field listField = ClassUtils.getField(TestDtoWithList.class, "list");

        // When
        final Class<?> result = ClassUtils.getGenericType(listField);

        // Then
        assertEquals(String.class, result);
    }

    @Test
    void getGenericType_nonGenericField() {
        // Given
        final Field listField = ClassUtils.getField(TestDtoWithList.class, "rawList");

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> ClassUtils.getGenericType(listField));
    }

    @Test
    void getGenericType_basicField() {
        // Given
        final Field listField = ClassUtils.getField(TestDto.class, "name");

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> ClassUtils.getGenericType(listField));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getGenericTypes() {
        // Given
        final Field mapField = ClassUtils.getField(TestDtoWithMap.class, "map");

        // When
        final Class<?>[] result = ClassUtils.getGenericTypes(mapField);

        // Then
        assertEquals(2, result.length);
        assertEquals(String.class, result[0]);
        assertEquals(Long.class, result[1]);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getGenericTypes_parameterizedTypes() {
        // Given
        final Field mapField = ClassUtils.getField(TestDtoWithMap.class, "mapOfLists");

        // When
        final Class<?>[] result = ClassUtils.getGenericTypes(mapField);

        // Then
        assertEquals(2, result.length);
        assertEquals(String.class, result[0]);
        assertEquals(List.class, result[1]);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getGenericTypes_wildcardTypes() {
        // Given
        final Field mapField = ClassUtils.getField(TestDtoWithMap.class, "mapOfWildcards");

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> ClassUtils.getGenericTypes(mapField));
    }

    @Test
    void getProperty() {
        // Given
        final String propertyName = "name";

        // When
        final PropertyDescriptor result = ClassUtils.getProperty(TestDto.class, propertyName);

        // Then
        assertNotNull(result);
    }

    @Test
    void getProperty_noGettersOrSetters() {
        // Given
        final String propertyName = "age";

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> ClassUtils.getProperty(TestDto.class, propertyName));
    }

    @Test
    void getProperty_notFound() {
        // Given
        final String propertyName = "abc123";

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> ClassUtils.getProperty(TestDto.class, propertyName));
    }

    @Test
    void getProperty_introSpectionException() {
        // Given
        final String propertyName = "name";

        try (final MockedStatic<Introspector> mockedIntrospector = mockStatic(Introspector.class)) {
            mockedIntrospector.when(() -> Introspector.getBeanInfo(TestDto.class))
                    .thenThrow(new IntrospectionException("test exception"));

            // When/Then
            assertThrows(IllegalStateException.class, () -> ClassUtils.getProperty(TestDto.class, propertyName));
        }
    }

    static class TestDto {
        private static String staticField;
        private String name;
        private int age;

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }
    }

    static class TestDtoWithList {
        private List<String> list;
        private List rawList;
    }

    static class TestDtoWithMap {
        private Map<String, Long> map;
        private Map<String, List<String>> mapOfLists;
        private Map<String, ?> mapOfWildcards;
    }

    static class ChildTestDto extends TestDto {
        private boolean active;
    }

    enum TestEnum {
        VALUE_A,
        VALUE_B
    }
}