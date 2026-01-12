package org.litebridge.tracking;

import org.junit.jupiter.api.Test;
import org.litebridge.commons.ClassUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FieldAccessorImplTest {

    @Test
    void name() {
        // Given
        final FieldAccessorImpl fieldAccessor = new FieldAccessorImpl(ClassUtils.getField(TestDto.class, "myVar"));

        // When
        final String result = fieldAccessor.name();

        // Then
        assertEquals("myVar", result);
    }

    @Test
    void get() {
        // Given
        final FieldAccessorImpl fieldAccessor = new FieldAccessorImpl(ClassUtils.getField(TestDto.class, "myVar"));
        final TestDto testDto = new TestDto();
        testDto.myVar = "testValue";

        // When
        final String result = (String) fieldAccessor.get(testDto);

        // Then
        assertEquals("testValue", result);
    }

    @Test
    void get_illegalArgumentException() {
        // Given
        final FieldAccessorImpl fieldAccessor = new FieldAccessorImpl(ClassUtils.getField(TestDto.class, "myVar"));
        final TestDto2 testDto2 = new TestDto2();

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> fieldAccessor.get(testDto2));
    }

    @Test
    void get_privateFieldFromAnotherClass() throws NoSuchFieldException {
        // Given
        final FieldAccessorImpl fieldAccessor = new FieldAccessorImpl(ClassUtils.getField(TestDto.class, "myVar"));
        final TestDifferentClass testDifferentClass = new TestDifferentClass();

        // When
        assertThrows(IllegalArgumentException.class, () -> fieldAccessor.get(testDifferentClass));
    }

    @Test
    void get_nullFieldValue() {
        // Given
        final FieldAccessorImpl fieldAccessor = new FieldAccessorImpl(ClassUtils.getField(TestDto.class, "myVar"));
        final TestDto testDto = new TestDto();

        // When
        final Object result = fieldAccessor.get(testDto);

        // Then
        assertEquals(null, result);
    }

    @Test
    void get_onNullObject() {
        // Given
        final FieldAccessorImpl fieldAccessor = new FieldAccessorImpl(ClassUtils.getField(TestDto.class, "myVar"));

        // When/Then
        assertThrows(NullPointerException.class, () -> fieldAccessor.get(null));
    }

    @Test
    void get_illegalAccessException() throws Exception {
        // Given
        final TestDto testDto = new TestDto();
        final Field mockField = mock(Field.class);
        final FieldAccessorImpl fieldAccessor = new FieldAccessorImpl(mockField);
        when(mockField.get(testDto)).thenThrow(IllegalAccessException.class);

        // When/Then
        assertThrows(IllegalStateException.class, () -> fieldAccessor.get(testDto));
    }

    @Test
    void set() {
        // Given
        final FieldAccessorImpl fieldAccessor = new FieldAccessorImpl(ClassUtils.getField(TestDto.class, "myVar"));
        final TestDto testDto = new TestDto();

        // When
        fieldAccessor.set(testDto, "testValue2");

        // Then
        assertEquals("testValue2", testDto.myVar);
    }

    @Test
    void set_illegalArgumentException() {
        // Given
        final FieldAccessorImpl fieldAccessor = new FieldAccessorImpl(ClassUtils.getField(TestDto.class, "myVar"));
        final TestDto2 testDto2 = new TestDto2();

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> fieldAccessor.set(testDto2, "invalid"));
    }

    @Test
    void set_illegalAccessException() throws Exception {
        // Given
        final TestDto testDto = new TestDto();
        final Field mockField = mock(Field.class);
        final FieldAccessorImpl fieldAccessor = new FieldAccessorImpl(mockField);
        doThrow(IllegalAccessException.class).when(mockField).set(testDto, "invalid");

        // When/Then
        assertThrows(IllegalStateException.class, () -> fieldAccessor.set(testDto, "invalid"));
    }

    @Test
    void type() {
        // Given
        final FieldAccessorImpl fieldAccessor = new FieldAccessorImpl(ClassUtils.getField(TestDto.class, "myVar"));

        // When
        final Class<?> result = fieldAccessor.type();

        // Then
        assertEquals(String.class, result);
    }

    @Test
    void genericTypes() {
        // Given
        final FieldAccessorImpl fieldAccessor = new FieldAccessorImpl(ClassUtils.getField(TestDto.class, "list"));

        // When
        final Class<?>[] result = fieldAccessor.genericTypes();

        // Then
        assertEquals(1, result.length);
        assertEquals(String.class, result[0]);
    }

    @Test
    void genericType() {
        // Given
        final FieldAccessorImpl fieldAccessor = new FieldAccessorImpl(ClassUtils.getField(TestDto.class, "list"));

        // When
        final Class<?> result = fieldAccessor.genericType();

        // Then
        assertEquals(String.class, result);
    }

    @Test
    void genericType_invalid() {
        // Given
        final FieldAccessorImpl fieldAccessor = new FieldAccessorImpl(ClassUtils.getField(TestDto.class, "map"));

        // When/Then
        assertThrows(IllegalStateException.class, fieldAccessor::genericType);
    }

    @Test
    void dtoClass() {// Given
        final FieldAccessorImpl fieldAccessor = new FieldAccessorImpl(ClassUtils.getField(TestDto.class, "myVar"));

        // When
        final Class<?> result = fieldAccessor.dtoClass();

        // Then
        assertEquals(TestDto.class, result);
    }

    @Test
    void equals_true_sameField() {
        // Given
        final FieldAccessorImpl fieldAccessor1 = new FieldAccessorImpl(ClassUtils.getField(TestDto.class, "myVar"));
        final FieldAccessorImpl fieldAccessor2 = new FieldAccessorImpl(ClassUtils.getField(TestDto.class, "myVar"));

        // When
        final boolean result = fieldAccessor1.equals(fieldAccessor2);

        // Then
        assertTrue(result);
    }

    @Test
    void equals_false_differentField() {
        // Given
        final FieldAccessorImpl fieldAccessor1 = new FieldAccessorImpl(ClassUtils.getField(TestDto.class, "myVar"));
        final FieldAccessorImpl fieldAccessor2 = new FieldAccessorImpl(ClassUtils.getField(TestDto.class, "list"));

        // When
        final boolean result = fieldAccessor1.equals(fieldAccessor2);

        // Then
        assertFalse(result);
    }

    @Test
    void equals_false_null() {
        // Given
        final FieldAccessorImpl fieldAccessor = new FieldAccessorImpl(ClassUtils.getField(TestDto.class, "myVar"));

        // When
        final boolean result = fieldAccessor.equals(null);

        // Then
        assertFalse(result);
    }

    @Test
    void equals_false_differentType() {
        // Given
        final FieldAccessorImpl fieldAccessor = new FieldAccessorImpl(ClassUtils.getField(TestDto.class, "myVar"));

        // When
        final boolean result = fieldAccessor.equals("not-a-field-accessor");

        // Then
        assertFalse(result);
    }

    @Test
    void hashCode_sameForEqualObjects() {
        // Given
        final FieldAccessorImpl fieldAccessor1 = new FieldAccessorImpl(ClassUtils.getField(TestDto.class, "myVar"));
        final FieldAccessorImpl fieldAccessor2 = new FieldAccessorImpl(ClassUtils.getField(TestDto.class, "myVar"));

        // When
        final int hash1 = fieldAccessor1.hashCode();
        final int hash2 = fieldAccessor2.hashCode();

        // Then
        assertEquals(hash1, hash2);
    }

    private class TestDto {
        private String myVar;
        private List<String> list;
        private Map<String, Long> map;
    }

    private class TestDto2 {
    }

    private class TestDifferentClass {
        private String myVar;
    }
}