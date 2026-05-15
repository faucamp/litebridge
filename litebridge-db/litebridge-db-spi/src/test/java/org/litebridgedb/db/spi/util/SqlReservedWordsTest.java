package org.litebridgedb.db.spi.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SqlReservedWordsTest {

    @Test
    void contains_withReservedWord_uppercase() {
        // Given
        String word = "SELECT";

        // When
        boolean result = SqlReservedWords.contains(word);

        // Then
        assertTrue(result, "SELECT should be identified as a reserved word.");
    }

    @Test
    void contains_withReservedWord_lowercase() {
        // Given
        String word = "select";

        // When
        boolean result = SqlReservedWords.contains(word);

        // Then
        assertTrue(result, "select (case insensitive) should be identified as a reserved word.");
    }

    @Test
    void contains_withNonReservedWord() {
        // Given
        String word = "MYCUSTOMWORD";

        // When
        boolean result = SqlReservedWords.contains(word);

        // Then
        assertFalse(result, "MYCUSTOMWORD is not a reserved word and should return false.");
    }

    @Test
    void contains_withMixedCaseReservedWord() {
        // Given
        String word = "CrEaTe";

        // When
        boolean result = SqlReservedWords.contains(word);

        // Then
        assertTrue(result, "CrEaTe should be identified as a reserved word (case insensitive).");
    }

    @Test
    void contains_withNullInput() {
        // Given
        String word = null;

        // When & Then
        assertThrows(NullPointerException.class, () -> SqlReservedWords.contains(word),
                "NullPointerException is expected when checking a null word.");
    }

    @Test
    void contains_withEmptyString() {
        // Given
        String word = "";

        // When
        boolean result = SqlReservedWords.contains(word);

        // Then
        assertFalse(result, "Empty string is not a reserved word and should return false.");
    }

    @Test
    void contains_withWhitespace() {
        // Given
        String word = " ";

        // When
        boolean result = SqlReservedWords.contains(word);

        // Then
        assertFalse(result, "Whitespace is not a reserved word and should return false.");
    }
}