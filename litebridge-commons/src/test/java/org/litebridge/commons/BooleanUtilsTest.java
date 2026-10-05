package org.litebridge.commons;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BooleanUtilsTest {

    @Test
    void toBoolean_true() {
        // Given
        final Boolean value = Boolean.TRUE;

        // When
        final boolean result = BooleanUtils.toBoolean(value);

        // Then
        assertTrue(result);
    }

    @Test
    void toBoolean_false() {
        // Given
        final Boolean value = Boolean.FALSE;

        // When
        final boolean result = BooleanUtils.toBoolean(value);

        // Then
        assertFalse(result);
    }

    @Test
    void toBoolean_null() {
        // Given
        final Boolean value = null;

        // When
        final boolean result = BooleanUtils.toBoolean(value);

        // Then
        assertFalse(result);
    }

    @Test
    void requireTrue_true() {
        // Given
        final boolean input = true;
        final String message = "Not used";

        // When
        final boolean result = BooleanUtils.requireTrue(input, message);

        // Then
        assertTrue(result);
    }

    @Test
    void requireTrue_false() {
        // Given
        final boolean input = false;
        final String message = "Test succeeded";

        // When
        final IllegalArgumentException result = assertThrows(IllegalArgumentException.class, () -> BooleanUtils.requireTrue(input, message));

        // Then
        assertEquals(message, result.getMessage());
    }

    @Test
    void requireFalse_false() {
        // Given
        final boolean input = false;
        final String message = "Not used";

        // When
        final boolean result = BooleanUtils.requireFalse(input, message);

        // Then
        assertFalse(result);
    }

    @Test
    void requireFalse_true() {
        // Given
        final boolean input = true;
        final String message = "Test succeeded";

        // When
        final IllegalArgumentException result = assertThrows(IllegalArgumentException.class, () -> BooleanUtils.requireFalse(input, message));

        // Then
        assertEquals(message, result.getMessage());
    }
}