package org.litebridge.commons;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class ObjectUtilsTest {

    /**
     * Tests that a non-null object is returned as-is when passed to requireNonNull.
     */
    @Test
    void requireNonNull() {
        // Given
        final String input = "Test String";
        final String message = "Object is null";

        // When
        final String result = assertDoesNotThrow(() -> ObjectUtils.requireNonNull(input, message));

        // Then
        assertEquals(input, result);
    }

    /**
     * Tests that requireNonNull throws an IllegalArgumentException when a null object is passed.
     */
    @Test
    void requireNonNull_null() {
        // Given
        final Object input = null;
        final String message = "Object must not be null";

        // When
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> ObjectUtils.requireNonNull(input, message));

        // Then
        assertEquals(message, exception.getMessage());
    }

    /**
     * Tests that requireNonNull correctly handles null as a message when throwing an exception.
     */
    @Test
    void requireNonNull_nullMessage() {
        // Given
        final Object input = null;
        final String message = null;

        // When
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> ObjectUtils.requireNonNull(input, message));

        // Then
        assertEquals(null, exception.getMessage());
    }

    @Test
    void requireNull() {
        // Given
        final Object input = null;

        // When
        ObjectUtils.requireNull(input, () -> new IllegalArgumentException("Object must be null"));
    }

    @Test
    void requireNull_notNull() {
        // Given
        final Object input = new Object();

        // When
        IllegalArgumentException result = assertThrows(IllegalArgumentException.class,
                () -> ObjectUtils.requireNull(input, () -> new IllegalArgumentException("Test message")));

        // Then
        assertEquals("Test message", result.getMessage());
    }

    @Test
    void getFieldValue() {
        // Given
        final TestDto testDto = new TestDto();
        testDto.myVar = "Hello World!";

        // When
        final String result = ObjectUtils.getFieldValue(testDto, "myVar", String.class);

        // Then
        assertEquals("Hello World!", result);
    }

    @Test
    void getFieldValue_invalidField() {
        // Given
        final TestDto testDto = new TestDto();
        testDto.myVar = "Hello World!";

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> ObjectUtils.getFieldValue(testDto, "nonExistingField", String.class));
    }

    @Test
    void getFieldValue_illegalAccessException() throws Exception {
        // Given
        final TestDto dto = new TestDto();
        final Field field = mock(Field.class);

        try (MockedStatic<ClassUtils> classUtils = mockStatic(ClassUtils.class)) {
            classUtils.when(() -> ClassUtils.getField(TestDto.class, "value"))
                    .thenReturn(field);

            when(field.get(dto)).thenThrow(new IllegalAccessException("forced"));

            // When / Then
            final IllegalStateException ex = assertThrows(
                    IllegalStateException.class,
                    () -> ObjectUtils.getFieldValue(dto, "value", String.class)
            );

            assertTrue(ex.getMessage().contains("Failed to get field"));
        }
    }

    private static class TestDto {
        private String myVar;
    }
}