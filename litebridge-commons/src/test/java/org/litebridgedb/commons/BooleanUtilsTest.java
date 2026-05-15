package org.litebridgedb.commons;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
}