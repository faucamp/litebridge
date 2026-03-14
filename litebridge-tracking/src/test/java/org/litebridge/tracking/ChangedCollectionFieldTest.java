package org.litebridge.tracking;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChangedCollectionFieldTest {

    @Test
    void listSnapshot() {
        // Given
        final ChangedCollectionField changedField = new ChangedCollectionField(
                "testName",
                List.of("value1", "value2"),
                List.of(0, 1),
                List.of(0)
        );

        // When
        final List<Integer> result = changedField.listSnapshot();

        // Then
        assertEquals(List.of(0, 1), result);
    }

    @Test
    void prevListSnapshot() {
        // Given
        final ChangedCollectionField changedField = new ChangedCollectionField(
                "testName",
                List.of("value1", "value2"),
                List.of(0, 1),
                List.of(0)
        );

        // When
        final List<Integer> result = changedField.prevListSnapshot();

        // Then
        assertEquals(List.of(0), result);
    }

    @Test
    void updatedIndices() {
        // Given
        final ChangedCollectionField changedField = new ChangedCollectionField(
                "testName",
                List.of("value0", "value1", "value2", "value3"),
                List.of(10, 20, 30, 40),
                List.of(10, 99, 30)
        );

        // When
        final List<Integer> result = changedField.updatedIndices();

        // Then
        assertEquals(List.of(1, 3), result);
    }

    @Test
    void updatedIndices_empty() {
        // Given
        final ChangedCollectionField changedField = new ChangedCollectionField(
                "testName",
                List.of("value0", "value1"),
                List.of(10, 20),
                List.of(10, 20)
        );

        // When
        final List<Integer> result = changedField.updatedIndices();

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void updatedValues() {
        // Given
        final ChangedCollectionField changedField = new ChangedCollectionField(
                "testName",
                List.of("value0", "value1", "value2", "value3"),
                List.of(10, 20, 30, 40),
                List.of(10, 99, 30)
        );

        // When
        final List<Object> result = changedField.updatedValues();

        // Then
        assertEquals(List.of("value1", "value3"), result);
    }

    @Test
    void updatedValues_nullValue() {
        // Given
        final ChangedCollectionField changedField = new ChangedCollectionField(
                "testName",
                null,
                List.of(0),
                List.of()
        );

        // When/Then
        assertThrows(NullPointerException.class, changedField::updatedValues);
    }

    @Test
    void testEquals_true() {
        // Given
        final ChangedCollectionField changedField1 = new ChangedCollectionField(
                "testName",
                List.of("value1", "value2"),
                List.of(0, 1),
                List.of(0)
        );
        final ChangedCollectionField changedField2 = new ChangedCollectionField(
                "testName",
                List.of("value1", "value2"),
                List.of(0, 1),
                List.of(99, 100)
        );

        // When
        final boolean result = changedField1.equals(changedField2);

        // Then
        assertTrue(result);
    }

    @Test
    void testEquals_sameObject() {
        // Given
        final ChangedCollectionField changedField = new ChangedCollectionField(
                "testName",
                List.of("value1"),
                List.of(0),
                List.of()
        );

        // When
        final boolean result = changedField.equals(changedField);

        // Then
        assertTrue(result);
    }

    @Test
    void testEquals_false_valueDiff() {
        // Given
        final ChangedCollectionField changedField1 = new ChangedCollectionField(
                "testName",
                List.of("value1", "value2"),
                List.of(0, 1),
                List.of(0)
        );
        final ChangedCollectionField changedField2 = new ChangedCollectionField(
                "testName",
                List.of("other1", "other2"),
                List.of(0, 1),
                List.of(0)
        );

        // When
        final boolean result = changedField1.equals(changedField2);

        // Then
        assertFalse(result);
    }

    @Test
    void testEquals_false_listSnapshotDiff() {
        // Given
        final ChangedCollectionField changedField1 = new ChangedCollectionField(
                "testName",
                List.of("value1", "value2"),
                List.of(0, 1),
                List.of(0)
        );
        final ChangedCollectionField changedField2 = new ChangedCollectionField(
                "testName",
                List.of("value1", "value2"),
                List.of(0, 2),
                List.of(0)
        );

        // When
        final boolean result = changedField1.equals(changedField2);

        // Then
        assertFalse(result);
    }

    @Test
    void testEquals_false_nameDiff() {
        // Given
        final ChangedCollectionField changedField1 = new ChangedCollectionField(
                "testName1",
                List.of("value1", "value2"),
                List.of(0, 1),
                List.of(0)
        );
        final ChangedCollectionField changedField2 = new ChangedCollectionField(
                "testName2",
                List.of("value1", "value2"),
                List.of(0, 1),
                List.of(0)
        );

        // When
        final boolean result = changedField1.equals(changedField2);

        // Then
        assertFalse(result);
    }

    @Test
    void testEquals_null() {
        // Given
        final ChangedCollectionField changedField = new ChangedCollectionField(
                "testName",
                List.of("value1"),
                List.of(0),
                List.of()
        );

        // When
        final boolean result = changedField.equals(null);

        // Then
        assertFalse(result);
    }

    @Test
    void testEquals_differentType() {
        // Given
        final ChangedCollectionField changedField = new ChangedCollectionField(
                "testName",
                List.of("value1"),
                List.of(0),
                List.of()
        );

        // When
        final boolean result = changedField.equals(new Object());

        // Then
        assertFalse(result);
    }

    @Test
    void testHashCode() {
        assertTrue(new ChangedCollectionField(
                "testName",
                List.of("value1", "value2"),
                List.of(0, 1),
                List.of(0)
        ).hashCode() != 0);
    }

    @Test
    void testToString() {
        assertNotNull(new ChangedCollectionField(
                "testName",
                List.of("value1", "value2"),
                List.of(0, 1),
                List.of(0)
        ).toString());
    }

    @Test
    void constructor_nullLists() {
        // When
        final ChangedCollectionField result = new ChangedCollectionField("testName", null, null, null);

        // Then
        assertNotNull(result.listSnapshot());
        assertTrue(result.listSnapshot().isEmpty());
        assertNotNull(result.prevListSnapshot());
        assertTrue(result.prevListSnapshot().isEmpty());
    }

    @Test
    void constructor_wrapsListsAsUnmodifiable() {
        // Given
        final List<Integer> listSnapshot = new ArrayList<>(List.of(0, 1));
        final List<Integer> prevListSnapshot = new ArrayList<>(List.of(0));

        // When
        final ChangedCollectionField result = new ChangedCollectionField(
                "testName",
                List.of("value1", "value2"),
                listSnapshot,
                prevListSnapshot
        );

        // Then
        assertThrows(UnsupportedOperationException.class, () -> result.listSnapshot().add(2));
        assertThrows(UnsupportedOperationException.class, () -> result.prevListSnapshot().add(1));
    }
}