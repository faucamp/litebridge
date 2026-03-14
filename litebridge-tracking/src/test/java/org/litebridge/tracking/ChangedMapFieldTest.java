package org.litebridge.tracking;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChangedMapFieldTest {

    @Test
    void mapSnapshot() {
        // Given
        final ChangedMapField changedField = new ChangedMapField("testName", Map.of("testKey", "testValue"), Map.of("testKey", 123));

        // When
        final Map<?, Integer> result = changedField.mapSnapshot();

        // Then
        assertEquals(Map.of("testKey", 123), result);
    }

    @Test
    void testEquals_true() {
        // Given
        final ChangedMapField changedField1 = new ChangedMapField("testName", Map.of("testKey", "testValue"), Map.of("testKey", 123));
        final ChangedMapField changedField2 = new ChangedMapField("testName", Map.of("testKey", "testValue"), Map.of("testKey", 123));

        // When
        final boolean result = changedField1.equals(changedField2);

        // Then
        assertTrue(result);
    }

    @Test
    void testEquals_sameObject() {
        // Given
        final ChangedMapField changedField1 = new ChangedMapField("testName", Map.of("testKey", "testValue"), Map.of("testKey", 123));

        // When
        final boolean result = changedField1.equals(changedField1);

        // Then
        assertTrue(result);
    }

    @Test
    void testEquals_false_valueDiff() {
        // Given
        final ChangedMapField changedField1 = new ChangedMapField("testName", Map.of("testKey", "testValue"), Map.of("testKey", 123));
        final ChangedMapField changedField2 = new ChangedMapField("testName", Map.of("testKey", "testValue2"), Map.of("testKey", 234));

        // When
        final boolean result = changedField1.equals(changedField2);

        // Then
        assertFalse(result);
    }

    @Test
    void testEquals_false_mapSnapshotDiff() {
        // Given
        final ChangedMapField changedField1 = new ChangedMapField("testName", Map.of("testKey", "testValue"), Map.of("testKey", 123));
        final ChangedMapField changedField2 = new ChangedMapField("testName", Map.of("testKey", "testValue"), Map.of("randomKey", 123));

        // When
        final boolean result = changedField1.equals(changedField2);

        // Then
        assertFalse(result);
    }

    @Test
    void testEquals_false_nameDiff() {
        // Given
        final ChangedMapField changedField1 = new ChangedMapField("testName1", Map.of("testKey", "testValue"), Map.of("testKey", 123));
        final ChangedMapField changedField2 = new ChangedMapField("testName2", Map.of("testKey", "testValue2"), Map.of("testKey", 234));

        // When
        final boolean result = changedField1.equals(changedField2);

        // Then
        assertFalse(result);
    }

    @Test
    void testEquals_null() {
        // Given
        final ChangedMapField changedField = new ChangedMapField("testName", Map.of("testKey", "testValue"), Map.of("testKey", 123));

        // When
        final boolean result = changedField.equals(null);

        // Then
        assertFalse(result);
    }

    @Test
    void testEquals_differentType() {
        // Given
        final ChangedMapField changedField = new ChangedMapField("testName", Map.of("testKey", "testValue"), Map.of("testKey", 123));

        // When
        final boolean result = changedField.equals(new Object());

        // Then
        assertFalse(result);
    }

    @Test
    void testHashCode() {
        assertTrue(new ChangedMapField("testName", Map.of("testKey", "testValue"), Map.of("testKey", 123)).hashCode() != 0);
    }

    @Test
    void testToString() {
        assertNotNull(new ChangedMapField("testName", Map.of("testKey", "testValue"), Map.of("testKey", 123)).toString());
    }

    @Test
    void constructor_nullMap() {
        // When
        final ChangedMapField result = new ChangedMapField("testName", null, null);

        // Then
        assertNotNull(result.mapSnapshot());
        assertTrue(result.mapSnapshot().isEmpty());
    }
}