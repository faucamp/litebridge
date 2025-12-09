package org.litebridge.commons;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
}