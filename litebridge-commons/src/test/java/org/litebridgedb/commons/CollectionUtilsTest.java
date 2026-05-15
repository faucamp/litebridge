package org.litebridgedb.commons;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CollectionUtilsTest {

    @Test
    void isEmpty_collection() {
        // Given
        final Collection<String> collection = List.of("test");

        // When
        final boolean result = CollectionUtils.isEmpty(collection);

        // Then
        assertFalse(result);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void isEmpty_collection_null() {
        // Given
        final Collection<?> collection = null;

        // When
        final boolean result = CollectionUtils.isEmpty(collection);

        // Then
        assertTrue(result);
    }

    @Test
    void isEmpty_collection_empty() {
        // Given
        final Collection<?> collection = Collections.emptyList();

        // When
        final boolean result = CollectionUtils.isEmpty(collection);

        // Then
        assertTrue(result);
    }

    @Test
    void isEmpty_map() {
        // Given
        final Map<String, String> map = Map.of("testKey", "testValue");

        // When
        final boolean result = CollectionUtils.isEmpty(map);

        // Then
        assertFalse(result);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void isEmpty_map_null() {
        // Given
        final Map<?, ?> map = null;

        // When
        final boolean result = CollectionUtils.isEmpty(map);

        // Then
        assertTrue(result);
    }

    @Test
    void isEmpty_map_empty() {
        // Given
        final Map<?, ?> map = Collections.emptyMap();

        // When
        final boolean result = CollectionUtils.isEmpty(map);

        // Then
        assertTrue(result);
    }

    @Test
    void isEmpty_iterable() {
        // Given
        final Iterable<String> iterable = List.of("test");

        // When
        final boolean result = CollectionUtils.isEmpty(iterable);

        // Then
        assertFalse(result);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void isEmpty_iterable_null() {
        // Given
        final Iterable<?> iterable = null;

        // When
        final boolean result = CollectionUtils.isEmpty(iterable);

        // Then
        assertTrue(result);
    }

    @Test
    void isEmpty_iterable_empty() {
        // Given
        final Iterable<?> iterable = Collections.emptyList();

        // When
        final boolean result = CollectionUtils.isEmpty(iterable);

        // Then
        assertTrue(result);
    }

    @Test
    void isEmpty_array() {
        // Given
        final String[] array = {"test"};

        // When
        final boolean result = CollectionUtils.isEmpty(array);

        // Then
        assertFalse(result);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    void isEmpty_array_null() {
        // Given
        final Object[] array = null;

        // When
        final boolean result = CollectionUtils.isEmpty(array);

        // Then
        assertTrue(result);
    }

    @Test
    void isEmpty_array_empty() {
        // Given
        final Object[] array = new Object[0];

        // When
        final boolean result = CollectionUtils.isEmpty(array);

        // Then
        assertTrue(result);
    }

    @Test
    void requireNonEmpty_collection() {
        // Given
        final Collection<String> collection = List.of("test");

        // When
        final Collection<String> result = assertDoesNotThrow(() -> CollectionUtils.requireNonEmpty(collection, "Test failed"));

        // Then
        assertEquals(collection, result);
    }

    @Test
    void requireNonEmpty_collection_empty() {
        // Given
        final Collection<String> collection = Collections.emptyList();
        final String message = "Test passed";

        // When
        final IllegalArgumentException result = assertThrows(IllegalArgumentException.class, () -> CollectionUtils.requireNonEmpty(collection, message));

        // Then
        assertEquals(message, result.getMessage());
    }

    @Test
    void requireNonEmpty_map() {
        // Given
        final Map<String, String> map = Map.of("testKey", "testValue");

        // When
        final Map<String, String> result = assertDoesNotThrow(() -> CollectionUtils.requireNonEmpty(map, "Test failed"));

        // Then
        assertEquals(map, result);
    }

    @Test
    void requireNonEmpty_map_empty() {
        // Given
        final Map<String, String> map = Collections.emptyMap();
        final String message = "Test passed";

        // When
        final IllegalArgumentException result = assertThrows(IllegalArgumentException.class, () -> CollectionUtils.requireNonEmpty(map, message));

        // Then
        assertEquals(message, result.getMessage());
    }

}