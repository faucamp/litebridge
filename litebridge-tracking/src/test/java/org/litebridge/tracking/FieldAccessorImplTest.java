package org.litebridge.tracking;

import org.junit.jupiter.api.Test;
import org.litebridge.commons.ClassUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    private class TestDto {
        private String myVar;
        private List<String> list;
        private Map<String, Long> map;
    }

    private class TestDto2 {
    }
}