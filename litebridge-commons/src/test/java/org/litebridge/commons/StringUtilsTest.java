package org.litebridge.commons;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StringUtilsTest {

    @Test
    void isBlank() {
        // Given
        final String input = "hello";

        // When
        final boolean result = StringUtils.isBlank(input);

        // Then
        assertFalse(result);
    }

    @Test
    void isBlank_null() {
        // Given
        String input = null;

        // When
        boolean result = StringUtils.isBlank(input);

        // Then
        assertTrue(result);
    }

    @Test
    void isBlank_empty() {
        // Given
        final String input = "";

        // When
        final boolean result = StringUtils.isBlank(input);

        // Then
        assertTrue(result);
    }

    @Test
    void isBlank_whitespace() {
        // Given
        final String input = " \t  ";

        // When
        final boolean result = StringUtils.isBlank(input);

        // Then
        assertTrue(result);
    }

    @Test
    void isBlank_whitespaceAndNonBlankCharacters() {
        // Given
        final String input = "  hello  ";

        // When
        final boolean result = StringUtils.isBlank(input);

        // Then
        assertFalse(result);
    }

    @Test
    void isEmpty() {
        // Given
        final String input = "hello";

        // When
        final boolean result = StringUtils.isEmpty(input);

        // Then
        assertFalse(result);
    }

    @Test
    void isEmpty_null() {
        // Given
        final String input = null;

        // When
        final boolean result = StringUtils.isEmpty(input);

        // Then
        assertTrue(result);
    }

    @Test
    void isEmpty_empty() {
        // Given
        final String input = "";

        // When
        final boolean result = StringUtils.isEmpty(input);

        // Then
        assertTrue(result);
    }

    @Test
    void isEmpty_whitespace() {
        // Given
        final String input = "   ";

        // When
        final boolean result = StringUtils.isEmpty(input);

        // Then
        assertFalse(result);
    }

    @Test
    void requireNonBlank() {
        // Given
        final String input = "hello";
        final String message = "Test failed";

        // When
        final String result = assertDoesNotThrow(() -> StringUtils.requireNonBlank(input, message));

        // Then
        assertEquals(input, result);
    }

    @Test
    void requireNonBlank_null() {
        // Given
        final String input = null;
        final String message = "Test passed";

        // When
        final IllegalArgumentException result = assertThrows(IllegalArgumentException.class, () -> StringUtils.requireNonBlank(input, message));

        // Then
        assertEquals(message, result.getMessage());
    }

    @Test
    void requireNonBlank_empty() {
        // Given
        final String input = "";
        final String message = "Test passed";

        // When
        final IllegalArgumentException result = assertThrows(IllegalArgumentException.class, () -> StringUtils.requireNonBlank(input, message));

        // Then
        assertEquals(message, result.getMessage());
    }

    @Test
    void requireNonBlank_whitespace() {
        // Given
        final String input = "   \t   ";
        final String message = "Test passed";

        // When
        final IllegalArgumentException result = assertThrows(IllegalArgumentException.class, () -> StringUtils.requireNonBlank(input, message));

        // Then
        assertEquals(message, result.getMessage());
    }

    @Test
    void splitOnce() {
        // Given
        final String input = "hello.world";
        final char delimiter = '.';

        // When
        final String[] result = StringUtils.splitOnce(input, delimiter);

        // Then
        assertEquals(2, result.length);
        assertEquals("hello", result[0]);
        assertEquals("world", result[1]);
    }

    @Test
    void splitOnce_multipleDelimiters() {
        // Given
        final String input = "hello.there.how.are.you";
        final char delimiter = '.';

        // When
        final String[] result = StringUtils.splitOnce(input, delimiter);

        // Then
        assertEquals(2, result.length);
        assertEquals("hello", result[0]);
        assertEquals("there.how.are.you", result[1]);
    }

    @Test
    void splitOnce_noDelimiter() {
        // Given
        final String input = "hello";
        final char delimiter = '.';

        // When
        final String[] result = StringUtils.splitOnce(input, delimiter);

        // Then
        assertEquals(1, result.length);
        assertEquals("hello", result[0]);
    }
}