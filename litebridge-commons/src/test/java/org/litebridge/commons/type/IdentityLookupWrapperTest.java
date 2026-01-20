package org.litebridge.commons.type;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IdentityLookupWrapperTest {

    /**
     * Test that two IdentityLookupWrapper objects with the same underlying object's reference are still false (since it's not part of the use case).
     */
    @Test
    void testEquals_sameReference() {
        // Given
        final Object obj = new Object();
        final WeakIdentityMap.IdentityLookupWrapper wrapper1 = new WeakIdentityMap.IdentityLookupWrapper(obj);
        final WeakIdentityMap.IdentityLookupWrapper wrapper2 = new WeakIdentityMap.IdentityLookupWrapper(obj);

        // When/Then
        assertNotEquals(wrapper1, wrapper2);
    }

    /**
     * Test that two IdentityLookupWrapper objects with different references are not equal.
     */
    @Test
    void testEquals_differentReferences() {
        // Given
        final Object obj1 = new Object();
        final Object obj2 = new Object();
        final WeakIdentityMap.IdentityLookupWrapper wrapper1 = new WeakIdentityMap.IdentityLookupWrapper(obj1);
        final WeakIdentityMap.IdentityLookupWrapper wrapper2 = new WeakIdentityMap.IdentityLookupWrapper(obj2);

        // When/Then
        assertFalse(wrapper1.equals(wrapper2));
    }

    /**
     * Test that an IdentityLookupWrapper is equal to an IdentityWeakReference if their underlying objects are the same.
     */
    @Test
    void testEquals_withIdentityWeakReference() {
        // Given
        final Object obj = new Object();
        final WeakIdentityMap.IdentityLookupWrapper wrapper = new WeakIdentityMap.IdentityLookupWrapper(obj);
        final WeakIdentityMap.IdentityWeakReference<Object> weakRef =
                new WeakIdentityMap.IdentityWeakReference<>(obj, null);

        // When/Then
        assertTrue(wrapper.equals(weakRef));
    }

    /**
     * Test that an IdentityLookupWrapper is not equal to an IdentityWeakReference if their underlying objects are different.
     */
    @Test
    void testEquals_withDifferentIdentityWeakReference() {
        // Given
        final Object obj1 = new Object();
        final Object obj2 = new Object();
        final WeakIdentityMap.IdentityLookupWrapper wrapper = new WeakIdentityMap.IdentityLookupWrapper(obj1);
        final WeakIdentityMap.IdentityWeakReference<Object> weakRef =
                new WeakIdentityMap.IdentityWeakReference<>(obj2, null);

        // When/Then
        assertFalse(wrapper.equals(weakRef));
    }

    /**
     * Test that an IdentityLookupWrapper is not equal to an object of a different class.
     */
    @Test
    void testEquals_differentClass() {
        // Given
        final Object obj = new Object();
        final WeakIdentityMap.IdentityLookupWrapper wrapper = new WeakIdentityMap.IdentityLookupWrapper(obj);

        // When/Then
        assertFalse(wrapper.equals("Some String"));
    }

    /**
     * Test that the hash code of IdentityLookupWrapper matches the identity hash code of the wrapped object.
     */
    @Test
    void testHashCode_matchesObjectIdentityHashCode() {
        // Given
        final Object obj = new Object();
        final WeakIdentityMap.IdentityLookupWrapper wrapper = new WeakIdentityMap.IdentityLookupWrapper(obj);

        // When/Then
        assertEquals(System.identityHashCode(obj), wrapper.hashCode());
    }
}