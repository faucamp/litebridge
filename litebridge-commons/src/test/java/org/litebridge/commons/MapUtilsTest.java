package org.litebridge.commons;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MapUtilsTest {

    @Test
    void testContainsKeyInNonNullMapKeyExists() {
        // Given
        Map<String, Integer> map = new HashMap<>();
        map.put("key1", 1);
        map.put("key2", 2);

        String keyToCheck = "key1";

        // When
        boolean result = MapUtils.containsKey(keyToCheck, map);

        // Then
        assertTrue(result, "The map should contain the specified key.");
    }

    @Test
    void testContainsKeyInNonNullMapKeyDoesNotExist() {
        // Given
        Map<String, Integer> map = new HashMap<>();
        map.put("key1", 1);
        map.put("key2", 2);

        String keyToCheck = "key3";

        // When
        boolean result = MapUtils.containsKey(keyToCheck, map);

        // Then
        assertFalse(result, "The map should not contain the specified key.");
    }

    @Test
    void testContainsKeyInEmptyMap() {
        // Given
        Map<String, Integer> map = Collections.emptyMap();
        String keyToCheck = "key1";

        // When
        boolean result = MapUtils.containsKey(keyToCheck, map);

        // Then
        assertFalse(result, "An empty map should not contain any keys.");
    }

    @Test
    void testContainsKeyInNullMap() {
        // Given
        Map<String, Integer> map = null;
        String keyToCheck = "key1";

        // When
        boolean result = MapUtils.containsKey(keyToCheck, map);

        // Then
        assertFalse(result, "A null map should not contain any keys.");
    }

    @Test
    void testContainsKeyWithNullKeyInMap() {
        // Given
        Map<String, Integer> map = new HashMap<>();
        map.put(null, 1);

        // When
        boolean result = MapUtils.containsKey(null, map);

        // Then
        assertTrue(result, "The map should contain the null key.");
    }

    @Test
    void testContainsKeyWithNullKeyAndNullMap() {
        // Given
        Map<String, Integer> map = null;

        // When
        boolean result = MapUtils.containsKey(null, map);

        // Then
        assertFalse(result, "A null map should not contain any keys, even null.");
    }
}