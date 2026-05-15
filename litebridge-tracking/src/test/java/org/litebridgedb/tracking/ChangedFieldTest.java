package org.litebridgedb.tracking;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChangedFieldTest {

    @Test
    void name() {
        // Given
        final ChangedField changedField = new ChangedField("testName", null);

        // When
        final String result = changedField.name();

        // Then
        assertEquals("testName", result);
    }

    @Test
    void value() {
        // Given
        final ChangedField changedField = new ChangedField("testName", "testValue");

        // When
        final Object result = changedField.value();

        // Then
        assertEquals("testValue", result);
    }

    @Test
    void cast() {
        // Given
        final ChangedField changedField = new ChangedMapField("testName", Map.of("testKey", "testValue"), Map.of("testKey", 123));

        // When
        final Optional<ChangedMapField> result = changedField.cast(ChangedMapField.class);

        // Then
        assertTrue(result.isPresent());
        assertEquals(changedField, result.get());
    }

    @Test
    void cast_failure() {
        // Given
        final ChangedField changedField = new ChangedField("testName", 123);

        // When
        final Optional<ChangedMapField> result = changedField.cast(ChangedMapField.class);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void testEquals_true() {
        // Given
        final ChangedField changedField1 = new ChangedField("testName", "testValue");
        final ChangedField changedField2 = new ChangedField("testName", "testValue");

        // When
        final boolean result = changedField1.equals(changedField2);

        // Then
        assertTrue(result);
    }

    @Test
    void testEquals_false_fieldNamesDiffer() {
        // Given
        final ChangedField changedField1 = new ChangedField("testName1", "testValue");
        final ChangedField changedField2 = new ChangedField("testName2", "testValue");

        // When
        final boolean result = changedField1.equals(changedField2);

        // Then
        assertFalse(result);
    }

    @Test
    void testEquals_false() {
        // Given
        final ChangedField changedField1 = new ChangedField("testName", "testValue1");
        final ChangedField changedField2 = new ChangedField("testName", "testValue2");

        // When
        final boolean result = changedField1.equals(changedField2);

        // Then
        assertFalse(result);
    }

    @Test
    void testEquals_null() {
        // Given
        final ChangedField changedField = new ChangedField("testName", "testValue");

        // When
        final boolean result = changedField.equals(null);

        // Then
        assertFalse(result);
    }

    @Test
    void testEquals_differentType() {
        // Given
        final ChangedField changedField = new ChangedField("testName", "testValue");

        // When
        final boolean result = changedField.equals(new Object());

        // Then
        assertFalse(result);
    }

    @Test
    void testEquals_sameObject() {
        // Given
        final ChangedField changedField = new ChangedField("testName", "testValue");

        // When
        final boolean result = changedField.equals(changedField);

        // Then
        assertTrue(result);
    }

    @Test
    void testHashCode() {
        assertTrue(new ChangedField("testName", "testValue").hashCode() > 0);
    }

    @Test
    void testToString() {
        assertNotNull(new ChangedField("testName", "testValue").toString());
    }
}