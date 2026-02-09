package org.litebridge.commons;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    @SuppressWarnings("ConstantConditions")
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
    @SuppressWarnings("ConstantConditions")
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

    @Test
    void abbreviate_basic() {
        // Given
        final String input = "input";

        // When
        final String result = StringUtils.abbreviate(input);

        // Then
        assertEquals("i", result);
    }

    @Test
    void abbreviate_capital() {
        // Given
        final String input = "Input";

        // When
        final String result = StringUtils.abbreviate(input);

        // Then
        assertEquals("i", result);
    }

    @Test
    void abbreviate_camelCase() {
        // Given
        final String input = "CamelCase";

        // When
        final String result = StringUtils.abbreviate(input);

        // Then
        assertEquals("cc", result);
    }

    @Test
    void abbreviate_capitalsUnderscore() {
        // Given
        final String input = "PERSON_ID";

        // When
        final String result = StringUtils.abbreviate(input);

        // Then
        assertEquals("pi", result);
    }

    @Test
    void abbreviate_spacesAndSpecialChars() {
        // Given
        final String input = " Hello\t World! camelCase test~char 123";

        // When
        final String result = StringUtils.abbreviate(input);

        // Then
        assertEquals("hwcctc123", result);
    }

    @Test
    void abbreviate_numbers() {
        // Given
        final String input = "Test123";

        // When
        final String result = StringUtils.abbreviate(input);

        // Then
        assertEquals("t123", result);
    }

    @Test
    void abbreviate_empty() {
        // Given
        final String input = "";

        // When
        final String result = StringUtils.abbreviate(input);

        // Then
        assertEquals("", result);
    }

    @Test
    void abbreviate_null() {
        // Given
        final String input = null;

        // When
        final String result = StringUtils.abbreviate(input);

        // Then
        assertEquals("", result);
    }

    @Test
    void abbreviate_unicode() {
        // Given
        final String input = "Hållø Wørl∂¡ camelCase 123";

        // When
        final String result = StringUtils.abbreviate(input);

        // Then
        assertEquals("hwcc123", result);
    }

    @Test
    void isAscii_true() {
        // Given
        final String input = "Hello World!";

        // When
        final boolean result = StringUtils.isAsciiOnly(input);

        // Then
        assertTrue(result);
    }

    @Test
    void isAscii_false() {
        // Given
        final String input = "å∫ç≈ß∂";

        // When
        final boolean result = StringUtils.isAsciiOnly(input);

        // Then
        assertFalse(result);
    }

    @Test
    void blankIfNull() {
        // Given
        final String input = null;

        // When
        final String result = StringUtils.blankIfNull(input);

        // Then
        assertEquals("", result);
    }

    @Test
    void blankIfNull_notNull() {
        // Given
        final String input = "Hello World!";

        // When
        final String result = StringUtils.blankIfNull(input);

        // Then
        assertEquals("Hello World!", result);
    }

    @Test
    void lowerFirst() {
        // Given
        final String input = "ABC DEF!";

        // When
        final String result = StringUtils.lowerFirst(input);

        // Then
        assertEquals("aBC DEF!", result);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void lowerFirst_null() {
        // Given
        final String input = null;

        // When
        assertThrows(IllegalArgumentException.class, () -> StringUtils.lowerFirst(input));
    }

    @Test
    void lowerFirst_empty() {
        // Given
        final String input = "";

        // When
        assertThrows(IllegalArgumentException.class, () -> StringUtils.lowerFirst(input));
    }

    @Test
    void camelCase() {
        // Given
        final String input = "Hello World 123!";

        // When
        final String result = StringUtils.camelCase(input);

        // Then
        assertEquals("helloWorld123", result);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void camelCase_null() {
        // When/Then
        assertThrows(NullPointerException.class, () -> StringUtils.camelCase(null));
    }

    @Test
    void camelCase_empty() {
        // Given
        final String input = "";

        // When
        final String result = StringUtils.camelCase(input);

        // Then
        assertEquals("", result);
    }

    @Test
    void split() {
        // Given
        final String input = "aaa.bbb.ccc.ddd";

        // When
        final List<String> result = StringUtils.split(input, '.');

        // Then
        assertEquals(4, result.size());
        assertEquals("aaa", result.get(0));
        assertEquals("bbb", result.get(1));
        assertEquals("ccc", result.get(2));
        assertEquals("ddd", result.get(3));
    }

    @Test
    void split_noMatch() {
        // Given
        final String input = "aaabbbcccddd";

        // When
        final List<String> result = StringUtils.split(input, '.');

        // Then
        assertEquals(1, result.size());
        assertEquals("aaabbbcccddd", result.getFirst());
    }

    @Test
    void split_emptyString() {
        // Given
        final String input = "";

        // When
        final List<String> result = StringUtils.split(input, '.');

        // Then
        assertEquals(1, result.size());
        assertEquals("", result.getFirst());
    }

    @Test
    void split_null() {
        // Given
        final String input = null;

        // When
        final List<String> result = StringUtils.split(input, '.');

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void splitArray() {
        // Given
        final String input = "aaa.bbb.ccc.ddd";

        // When
        final String[] result = StringUtils.splitArray(input, '.');

        // Then
        assertEquals(4, result.length);
        assertEquals("aaa", result[0]);
        assertEquals("bbb", result[1]);
        assertEquals("ccc", result[2]);
        assertEquals("ddd", result[3]);
    }

    @Test
    void split_setSize_truncate() {
        // Given
        final String input = "a.b.c.d";
        final int setSize = 2;
        final char delimiter = '.';

        // When
        final IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> StringUtils.split(input, delimiter, setSize, false));

        // Then
        assertEquals("Could not parse string: 'a.b.c.d'; expected 2 parts, got: 4", exception.getMessage());
    }

    @Test
    void split_setSize_padEmpty_start() {
        // Given
        final String input = "a.b";
        final int setSize = 4;
        final char delimiter = '.';
        final boolean padEmptyAtStart = true;

        // When
        final List<String> result = StringUtils.split(input, delimiter, setSize, padEmptyAtStart);

        // Then
        assertEquals(List.of("", "", "a", "b"), result);
    }

    @Test
    void split_setSize_padEmpty_end() {
        // Given
        final String input = "a.b";
        final int setSize = 4;
        final char delimiter = '.';
        final boolean padEmptyAtStart = false;

        // When
        final List<String> result = StringUtils.split(input, delimiter, setSize, padEmptyAtStart);

        // Then
        assertEquals(List.of("a", "b", "", ""), result);
    }

    @Test
    void split_setSize_noPadding() {
        // Given
        final String input = "a.b.c";
        final int setSize = 3;
        final char delimiter = '.';
        final boolean padEmptyAtStart = false;

        // When
        final List<String> result = StringUtils.split(input, delimiter, setSize, padEmptyAtStart);

        // Then
        assertEquals(List.of("a", "b", "c"), result);
    }

    @Test
    void splitArray_setSize_truncate() {
        // Given
        final String input = "a.b.c.d";
        final int setSize = 2;
        final char delimiter = '.';

        // When
        final IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> StringUtils.splitArray(input, delimiter, setSize, false));

        // Then
        assertEquals("Could not parse string: 'a.b.c.d'; expected 2 parts, got: 4", exception.getMessage());
    }

    @Test
    void splitArray_setSize_padEmpty_start() {
        // Given
        final String input = "a.b";
        final int setSize = 4;
        final char delimiter = '.';
        final boolean padEmptyAtStart = true;

        // When
        final String[] result = StringUtils.splitArray(input, delimiter, setSize, padEmptyAtStart);

        // Then
        assertArrayEquals(new String[]{"", "", "a", "b"}, result);
    }

    @Test
    void splitArray_setSize_padEmpty_end() {
        // Given
        final String input = "a.b";
        final int setSize = 4;
        final char delimiter = '.';
        final boolean padEmptyAtStart = false;

        // When
        final String[] result = StringUtils.splitArray(input, delimiter, setSize, padEmptyAtStart);

        // Then
        assertArrayEquals(new String[]{"a", "b", "", ""}, result);
    }

    @Test
    void splitArray_setSize_noPadding() {
        // Given
        final String input = "a.b.c";
        final int setSize = 3;
        final char delimiter = '.';
        final boolean padEmptyAtStart = false;

        // When
        final String[] result = StringUtils.splitArray(input, delimiter, setSize, padEmptyAtStart);

        // Then
        assertArrayEquals(new String[]{"a", "b", "c"}, result);
    }
}