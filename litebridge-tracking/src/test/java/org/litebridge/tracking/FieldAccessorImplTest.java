package org.litebridge.tracking;

import org.junit.jupiter.api.Test;
import org.litebridge.commons.ClassUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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

    private class TestDto {
        private String myVar;
        private List<String> list;
    }
}