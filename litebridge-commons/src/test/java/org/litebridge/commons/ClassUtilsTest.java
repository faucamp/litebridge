package org.litebridge.commons;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    static class TestDto {
        private String name;
        private int age;
    }

    static class TestDtoWithList {
        private List<String> list;
        private List rawList;
    }

    static class ChildTestDto extends TestDto {
        private boolean active;
    }

    enum TestEnum {
        VALUE_A,
        VALUE_B
    }
}