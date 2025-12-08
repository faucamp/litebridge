package org.litebridge.core;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClassUtilTest {

    @Test
    void getAllFields() {
        // When
        final List<Field> result = ClassUtil.getAllFields(TestDto.class);

        // Then
        assertEquals(2, result.size());
        assertEquals("name", result.get(0).getName());
        assertEquals("age", result.get(1).getName());
    }

    @Test
    void getAllFields_inheritance() {
        // When
        final List<Field> result = ClassUtil.getAllFields(ChildTestDto.class);

        // Then
        assertEquals(3, result.size());
        assertEquals("active", result.get(0).getName());
        assertEquals("name", result.get(1).getName());
        assertEquals("age", result.get(2).getName());
    }

    @Test
    void getField() {
        // Given
        final String fieldName = "age";

        // When
        final Field field = ClassUtil.getField(TestDto.class, fieldName);

        // Then
        assertNotNull(field);
        assertEquals(fieldName, field.getName());
    }

    @Test
    void getField_notFound() {
        // Given
        final String fieldName = "nonExistingField";

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> ClassUtil.getField(TestDto.class, fieldName));
    }

    @Test
    void getField_inheritedField() {
        // Given
        final String fieldName = "age";

        // When
        final Field field = ClassUtil.getField(ChildTestDto.class, fieldName);

        // Then
        assertNotNull(field);
        assertEquals(fieldName, field.getName());
    }

    @Test
    void getField_inheritance_notFound() {
        // Given
        final String fieldName = "nonExistingField";

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> ClassUtil.getField(ChildTestDto.class, fieldName));
    }

    @Test
    void isBasicType_String() {
        // Given
        final String object = "Hello World!";

        // When
        final boolean result = ClassUtil.isBasicType(object.getClass());

        // Then
        assertTrue(result);
    }

    @Test
    void isBasicType_Short() {
        // Given
        final Short object = Short.valueOf((short) 1);

        // When
        final boolean result = ClassUtil.isBasicType(object.getClass());

        // Then
        assertTrue(result);
    }

    @Test
    void isBasicType_Integer() {
        // Given
        final Integer object = Integer.valueOf(1);

        // When
        final boolean result = ClassUtil.isBasicType(object.getClass());

        // Then
        assertTrue(result);
    }

    @Test
    void isBasicType_int() {
        // Given
        final Field ageField = ClassUtil.getField(TestDto.class, "age");

        // When
        final boolean result = ClassUtil.isBasicType(ageField.getType());

        // Then
        assertTrue(result);
    }

    @Test
    void isBasicType_Long() {
        // Given
        final Long object = Long.valueOf(1);

        // When
        final boolean result = ClassUtil.isBasicType(object.getClass());

        // Then
        assertTrue(result);
    }

    @Test
    void isBasicType_BigDecimal() {
        // Given
        final BigDecimal object = BigDecimal.valueOf(1);

        // When
        final boolean result = ClassUtil.isBasicType(object.getClass());

        // Then
        assertTrue(result);
    }

    @Test
    void isBasicType_Double() {
        // Given
        final Double object = Double.valueOf(1.234);

        // When
        final boolean result = ClassUtil.isBasicType(object.getClass());

        // Then
        assertTrue(result);
    }

    @Test
    void isBasicType_Float() {
        // Given
        final Float object = Float.valueOf(1.234f);

        // When
        final boolean result = ClassUtil.isBasicType(object.getClass());

        // Then
        assertTrue(result);
    }

    @Test
    void isBasicType_Enum() {
        // When
        final boolean result = ClassUtil.isBasicType(TestEnum.class);

        // Then
        assertTrue(result);
    }

    @Test
    void isBasicType_Boolean() {
        // Given
        final Boolean object = Boolean.TRUE;

        // When
        final boolean result = ClassUtil.isBasicType(object.getClass());

        // Then
        assertTrue(result);
    }

    @Test
    void isBasicType_byteArray() {
        // Given
        final byte[] byteArray = new byte[1];

        // When
        final boolean result = ClassUtil.isBasicType(byteArray.getClass());

        // Then
        assertTrue(result);
    }

    static class TestDto {
        private String name;
        private int age;
    }

    static class ChildTestDto extends TestDto {
        private boolean active;
    }

    enum TestEnum {
        VALUE_A,
        VALUE_B
    }
}