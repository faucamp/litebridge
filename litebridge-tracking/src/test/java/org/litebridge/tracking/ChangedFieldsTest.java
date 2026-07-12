package org.litebridge.tracking;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChangedFieldsTest {

    private ChangedFields changedFields = new ChangedFields(Map.of("testName", new ChangedField("testName", "testValue")));

    @Test
    void get() {
        // When
        final Optional<ChangedField> result = changedFields.get("testName");

        // Then
        assertTrue(result.isPresent());
        assertEquals("testValue", result.get().value());
    }

    @Test
    void get_notFound() {
        // When
        final Optional<ChangedField> result = changedFields.get("invalid");

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void getOrNull() {
        // When
        final ChangedField result = changedFields.getOrNull("testName");

        // Then
        assertNotNull(result);
        assertEquals("testValue", result.value);
    }

    @Test
    void getOrNull_notFound() {
        // When
        final ChangedField result = changedFields.getOrNull("invalid");

        // Then
        assertNull(result);
    }

    @Test
    void stream() {
        // When
        final Stream<ChangedField> result = changedFields.stream();

        // Then
        assertEquals(1, result.count());
    }

    @Test
    void forEach() {
        // Given
        final int[] counter = {0};

        // When
        changedFields.forEach(changedField -> counter[0]++);

        // Then
        assertEquals(1, counter[0]);
    }

    @Test
    void isEmpty_true() {
        // Given
        final ChangedFields changedFields = new ChangedFields(Collections.emptyMap());

        // When
        final boolean result = changedFields.isEmpty();

        // Then
        assertTrue(result);
    }

    @Test
    void isEmpty_false() {
        // When
        final boolean result = changedFields.isEmpty();

        // Then
        assertFalse(result);
    }

    @Test
    void contains_true() {
        // When
        final boolean result = changedFields.contains("testName");

        // Then
        assertTrue(result);
    }

    @Test
    void contains_false() {
        // When
        final boolean result = changedFields.contains("invalid");

        // Then
        assertFalse(result);
    }

    @Test
    void size() {
        // When
        final int result = changedFields.size();

        // Then
        assertEquals(1, result);
    }
}