package org.litebridge.commons.type;

import org.junit.jupiter.api.Test;

import java.lang.ref.ReferenceQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class IdentityWeakReferenceTest {

    /**
     * Test that two IdentityWeakReference objects referring to the same object are equal.
     */
    @Test
    void testEquals_sameObject() {
        // Given
        final Object referent = new Object();
        final ReferenceQueue<Object> queue = new ReferenceQueue<>();
        final WeakIdentityMap.IdentityWeakReference<Object> ref1 = new WeakIdentityMap.IdentityWeakReference<>(referent, queue);
        final WeakIdentityMap.IdentityWeakReference<Object> ref2 = new WeakIdentityMap.IdentityWeakReference<>(referent, queue);

        // When/Then
        assertEquals(ref1, ref2, "Expected IdentityWeakReference objects to be equal when referring to the same object");
    }

    /**
     * Test that two IdentityWeakReference objects referring to different objects are not equal.
     */
    @Test
    void testEquals_differentObjects() {
        // Given
        final Object referent1 = new Object();
        final Object referent2 = new Object();
        final ReferenceQueue<Object> queue = new ReferenceQueue<>();
        final WeakIdentityMap.IdentityWeakReference<Object> ref1 = new WeakIdentityMap.IdentityWeakReference<>(referent1, queue);
        final WeakIdentityMap.IdentityWeakReference<Object> ref2 = new WeakIdentityMap.IdentityWeakReference<>(referent2, queue);

        // When/Then
        assertNotEquals(ref1, ref2, "Expected IdentityWeakReference objects to not be equal when referring to different objects");
    }

    /**
     * Test that two IdentityWeakReference objects referring to null are equal.
     */
    @Test
    void testEquals_bothReferencesNull() {
        // Given
        final ReferenceQueue<Object> queue = new ReferenceQueue<>();
        final WeakIdentityMap.IdentityWeakReference<Object> ref1 = new WeakIdentityMap.IdentityWeakReference<>(null, queue);
        final WeakIdentityMap.IdentityWeakReference<Object> ref2 = new WeakIdentityMap.IdentityWeakReference<>(null, queue);

        // When/Then
        assertEquals(ref1, ref2, "Expected IdentityWeakReference objects to be equal when both refer to null");
    }

    /**
     * Test that an IdentityWeakReference referring to null is not equal to one referring to an actual object.
     */
    @Test
    void testEquals_oneReferenceNull() {
        // Given
        final Object referent = new Object();
        final ReferenceQueue<Object> queue = new ReferenceQueue<>();
        final WeakIdentityMap.IdentityWeakReference<Object> ref1 = new WeakIdentityMap.IdentityWeakReference<>(referent, queue);
        final WeakIdentityMap.IdentityWeakReference<Object> ref2 = new WeakIdentityMap.IdentityWeakReference<>(null, queue);

        // When/Then
        assertNotEquals(ref1, ref2, "Expected IdentityWeakReference objects to not be equal when one refers to an object and the other to null");
    }

    /**
     * Test that an IdentityWeakReference is not equal to an object of a different class.
     */
    @Test
    void testEquals_differentClass() {
        // Given
        final Object referent = new Object();
        final ReferenceQueue<Object> queue = new ReferenceQueue<>();
        final WeakIdentityMap.IdentityWeakReference<Object> ref = new WeakIdentityMap.IdentityWeakReference<>(referent, queue);

        // When/Then
        assertNotEquals(ref, "SomeString", "Expected IdentityWeakReference to not be equal to an object of a different class");
    }

    /**
     * Test that the hashCode of an IdentityWeakReference matches the identity hash code of the referent.
     */
    @Test
    void testHashCode_matchesReferentIdentityHashCode() {
        // Given
        final Object referent = new Object();
        final ReferenceQueue<Object> queue = new ReferenceQueue<>();
        final WeakIdentityMap.IdentityWeakReference<Object> ref = new WeakIdentityMap.IdentityWeakReference<>(referent, queue);

        // When/Then
        assertEquals(System.identityHashCode(referent), ref.hashCode(), "Expected hashCode of IdentityWeakReference to match identity hash code of referent");
    }

    /**
     * Test that the hashCode of an IdentityWeakReference with a null referent returns the precomputed hash code.
     */
    @Test
    void testHashCode_withNullReferent() {
        // Given
        final ReferenceQueue<Object> queue = new ReferenceQueue<>();
        final WeakIdentityMap.IdentityWeakReference<Object> ref = new WeakIdentityMap.IdentityWeakReference<>(null, queue);

        // When
        int hash = ref.hashCode();

        // Then
        assertEquals(System.identityHashCode(null), hash, "Expected hashCode of IdentityWeakReference with null referent to match identity hash code of null");
    }

    /**
     * Test that an IdentityWeakReference referring to an object is equal to itself.
     */
    @Test
    void testEquals_sameInstance() {
        // Given
        final Object referent = new Object();
        final ReferenceQueue<Object> queue = new ReferenceQueue<>();
        final WeakIdentityMap.IdentityWeakReference<Object> ref = new WeakIdentityMap.IdentityWeakReference<>(referent, queue);

        // When/Then
        assertEquals(ref, ref, "Expected IdentityWeakReference to be equal to itself");
    }
}