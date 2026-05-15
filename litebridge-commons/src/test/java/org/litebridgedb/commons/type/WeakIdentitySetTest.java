package org.litebridgedb.commons.type;

import org.junit.jupiter.api.Test;

import java.lang.ref.WeakReference;
import java.time.Duration;
import java.util.Iterator;
import java.util.concurrent.locks.LockSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the {@link WeakIdentitySet} class.
 * This class ensures correct behavior for critical operations like {@code add}, {@code remove},
 * {@code contains}, and {@code size}. It also tests weak referencing behavior implicit in the set.
 */
class WeakIdentitySetTest {

    @Test
    void add_shouldReturnTrueWhenAddingNewElement() {
        // Given
        final WeakIdentitySet<Object> set = new WeakIdentitySet<>();
        final Object element = new Object();

        // When
        boolean result = set.add(element);

        // Then
        assertTrue(result, "Expected add to return true when adding a new element");
        assertTrue(set.contains(element), "Expected the set to contain the added element");
    }

    @Test
    void add_shouldReturnFalseWhenAddingDuplicateElement() {
        // Given
        final WeakIdentitySet<Object> set = new WeakIdentitySet<>();
        final Object element = new Object();
        set.add(element);

        // When
        boolean result = set.add(element);

        // Then
        assertFalse(result, "Expected add to return false when adding a duplicate element");
        assertEquals(1, set.size(), "Expected size to remain 1 after adding a duplicate element");
    }

    @Test
    void remove_shouldReturnTrueWhenElementExists() {
        // Given
        final WeakIdentitySet<Object> set = new WeakIdentitySet<>();
        final Object element = new Object();
        set.add(element);

        // When
        boolean result = set.remove(element);

        // Then
        assertTrue(result, "Expected remove to return true for an existing element");
        assertFalse(set.contains(element), "Expected the set to no longer contain the removed element");
    }

    @Test
    void remove_shouldReturnFalseWhenElementDoesNotExist() {
        // Given
        final WeakIdentitySet<Object> set = new WeakIdentitySet<>();
        final Object element = new Object();

        // When
        boolean result = set.remove(element);

        // Then
        assertFalse(result, "Expected remove to return false for a non-existing element");
    }

    @Test
    void contains_shouldReturnTrueForExistingElement() {
        // Given
        final WeakIdentitySet<Object> set = new WeakIdentitySet<>();
        final Object element = new Object();
        set.add(element);

        // When
        boolean result = set.contains(element);

        // Then
        assertTrue(result, "Expected contains to return true for an existing element");
    }

    @Test
    void contains_shouldReturnFalseForNonExistingElement() {
        // Given
        final WeakIdentitySet<Object> set = new WeakIdentitySet<>();
        final Object element = new Object();

        // When
        boolean result = set.contains(element);

        // Then
        assertFalse(result, "Expected contains to return false for a non-existing element");
    }

    @Test
    void size_shouldReturnZeroWhenEmpty() {
        // Given
        final WeakIdentitySet<Object> set = new WeakIdentitySet<>();

        // When
        final int result = set.size();

        // Then
        assertEquals(0, result, "Expected size to be 0 for an empty set");
    }

    @Test
    void size_shouldReturnCorrectNumberOfElements() {
        // Given
        final WeakIdentitySet<Object> set = new WeakIdentitySet<>();
        set.add(new Object());
        set.add(new Object());

        // When
        int size = set.size();

        // Then
        assertEquals(2, size, "Expected size to reflect the number of unique elements in the set");
    }

    @Test
    void size_shouldDecreaseWhenElementIsRemoved() {
        // Given
        final WeakIdentitySet<Object> set = new WeakIdentitySet<>();
        final Object element = new Object();
        set.add(element);

        // When
        set.remove(element);

        // Then
        assertEquals(0, set.size(), "Expected size to decrease after removing an element");
    }

    @Test
    void iterator_shouldIterateOverAllElements() {
        // Given
        final WeakIdentitySet<Object> set = new WeakIdentitySet<>();
        final Object element1 = new Object();
        final Object element2 = new Object();
        set.add(element1);
        set.add(element2);

        // When
        Iterator<Object> iterator = set.iterator();

        // Then
        assertTrue(iterator.hasNext(), "Expected iterator to have elements");
        final Object first = iterator.next();
        assertTrue(first == element1 || first == element2, "Expected iterator to return an element from the set");
        final Object second = iterator.next();
        assertTrue(second == element1 || second == element2, "Expected iterator to return all elements from the set");
        assertNotEquals(first, second, "Expected iterator to not return duplicate elements");
        assertFalse(iterator.hasNext(), "Expected iterator to signal end of elements");
    }

    @Test
    void elements_shouldBeRemovedWhenGarbageCollected() {
        // Given
        final WeakIdentitySet<Object> set = new WeakIdentitySet<>();
        final WeakReference<Object> weakRef = new WeakReference<>(new Object());
        Object element = weakRef.get();
        assertNotNull(element, "Object reference was expected to not be null");

        set.add(element);

        // When
        element = null; // Remove strong reference
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            awaitCollected(weakRef);
        });

        // Then
        assertEquals(0, set.size(), "Expected size to be 0 after garbage collecting the only element");
    }

    private static void awaitCollected(WeakReference<?> reference) {
        while (reference.get() != null) {
            System.gc();
            LockSupport.parkNanos(Duration.ofMillis(10).toNanos());
        }
    }
}