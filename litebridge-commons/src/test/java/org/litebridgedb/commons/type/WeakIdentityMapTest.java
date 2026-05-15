package org.litebridgedb.commons.type;

import org.junit.jupiter.api.Test;

import java.lang.ref.WeakReference;
import java.time.Duration;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.locks.LockSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WeakIdentityMapTest {

    /**
     * Test that put adds a key-value pair into the map and allows retrieval of that value using the key.
     */
    @Test
    void put() {
        // Given
        final WeakIdentityMap<Object, String> map = new WeakIdentityMap<>();
        final Object key = new Object();
        final String value = "Value1";

        // When
        String previousValue = map.put(key, value);

        // Then
        assertNull(previousValue);
        assertEquals(value, map.get(key));
    }

    /**
     * Test that put updates the value for an existing key.
     */
    @Test
    void put_updateExisting() {
        // Given
        final WeakIdentityMap<Object, String> map = new WeakIdentityMap<>();
        final Object key = new Object();
        final String initialValue = "InitialValue";
        final String updatedValue = "UpdatedValue";

        map.put(key, initialValue);

        // When
        final String previousValue = map.put(key, updatedValue);

        // Then
        assertEquals(initialValue, previousValue);
        assertEquals(updatedValue, map.get(key));
    }

    /**
     * Test that put stores multiple key-value pairs and allows retrieval of values using their respective keys.
     */
    @Test
    void put_multiple() {
        // Given
        final WeakIdentityMap<Object, String> map = new WeakIdentityMap<>();
        final Object key1 = new Object();
        final Object key2 = new Object();
        final String value1 = "Value1";
        final String value2 = "Value2";

        // When
        map.put(key1, value1);
        map.put(key2, value2);

        // Then
        assertEquals(value1, map.get(key1), "Expected to retrieve the value for key1");
        assertEquals(value2, map.get(key2), "Expected to retrieve the value for key2");
    }

    /**
     * Test that put identifies keys by identity rather than equality.
     */
    @Test
    void put_usesIdentityForKeys() {
        // Given
        final WeakIdentityMap<Object, String> map = new WeakIdentityMap<>();
        final ConstantHashObject key1 = new ConstantHashObject();
        final ConstantHashObject key2 = new ConstantHashObject();
        final String value1 = "Value1";
        final String value2 = "Value2";
        assertEquals(key1, key2);
        assertNotSame(key1, key2);

        // When
        map.put(key1, value1);
        map.put(key2, value2);

        // Then
        assertEquals(value1, map.get(key1));
        assertEquals(value2, map.get(key2));
        assertNotEquals(value1, value2);
    }

    /**
     * Test that a key is removed when it is no longer strongly referenced.
     */
    @Test
    void put_removesKeyWhenGarbageCollected() {
        // Given
        final WeakIdentityMap<Object, String> map = new WeakIdentityMap<>();
        final String value = "Value";

        // Put the key in a nested scope to reduce the chance the JIT keeps it alive
        final WeakReference<Object> keyRef = new WeakReference<>(new Object());
        Object key = keyRef.get();

        // When
        map.put(key, value);
        assertFalse(map.isEmpty());
        assertEquals(value, map.get(key), "Expected to retrieve the value for the key");

        // Drop the strong reference
        key = null;

        // Then
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            awaitCollected(keyRef);
            // Trigger expunge via a map operation and wait until it reflects the removal.
            while (!map.isEmpty()) {
                System.gc();
                LockSupport.parkNanos(Duration.ofMillis(10).toNanos());
            }
        });

        assertTrue(map.isEmpty(), "Expected map to be empty after key is garbage collected");
    }

    /**
     * Test that put returns null if an entry is removed due to a stale weak reference.
     */
    @Test
    void put_handlesStaleEntries() {
        // Given
        final WeakIdentityMap<Object, String> map = new WeakIdentityMap<>();
        // Put the key in a nested scope to reduce the chance the JIT keeps it alive
        final WeakReference<Object> keyRef = new WeakReference<>(new ConstantHashObject());
        Object key = keyRef.get();
        final String value = "Value";

        map.put(key, value);

        // Remove strong reference to the key
        key = null;

        // Then
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            awaitCollected(keyRef);
        });

        // When
        final String result = map.put(keyRef.get(), "NewValue");

        // Then
        assertNull(result, "Expected null to be returned from put if stale entries are removed");
    }

    @Test
    void remove() {
        // Given
        final WeakIdentityMap<Object, String> map = new WeakIdentityMap<>();
        final Object key = new Object();
        final String value = "Value";
        map.put(key, value);

        // When
        final String result = map.remove(key);

        // Then
        assertEquals(value, result);
        assertTrue(map.isEmpty());
    }

    @Test
    void containsKey() {
        // Given
        final WeakIdentityMap<Object, String> map = new WeakIdentityMap<>();
        final Object key = new Object();
        final String value = "Value";
        map.put(key, value);

        // When/Then
        assertTrue(map.containsKey(key));
        assertFalse(map.containsKey(new Object()));
    }

    @Test
    void size() {
        // Given
        final WeakIdentityMap<Object, String> map = new WeakIdentityMap<>();
        final Object key = new Object();
        final String value = "Value";
        map.put(key, value);

        // When/Then
        assertEquals(1, map.size());
    }

    @Test
    void isEmpty() {
        // Given
        final WeakIdentityMap<Object, String> map = new WeakIdentityMap<>();

        // When/Then
        assertTrue(map.isEmpty());
        map.put(new Object(), "Value");
        assertFalse(map.isEmpty());
    }

    @Test
    void clear() {
        // Given
        final WeakIdentityMap<Object, String> map = new WeakIdentityMap<>();
        final Object key = new Object();
        final String value = "Value";
        map.put(key, value);

        // When
        map.clear();

        // Then
        assertTrue(map.isEmpty());
    }

    @Test
    void clear_withStaleEntry() {
        final WeakIdentityMap<Object, String> map = new WeakIdentityMap<>();
        Object key1 = new Object();
        map.put(key1, "value1");

        final WeakReference<Object> ref = new WeakReference<>(key1);
        key1 = null;
        awaitCollected(ref);

        // At this point the ref should be in the queue
        map.clear();
        assertTrue(map.isEmpty());
    }

    @Test
    void putAll() {
        // Given
        final WeakIdentityMap<Object, String> map = new WeakIdentityMap<>();
        final WeakIdentityMap<Object, String> otherMap = new WeakIdentityMap<>();
        final Object key = new Object();
        final String value = "Value";
        otherMap.put(key, value);

        // When
        map.putAll(otherMap);

        // Then
        assertEquals(value, map.get(key));
    }

    @Test
    void containsValue() {
        // Given
        final WeakIdentityMap<Object, String> map = new WeakIdentityMap<>();
        final Object key = new Object();
        final String value = "Value";
        map.put(key, value);

        // When/Then
        assertTrue(map.containsValue(value));
        assertFalse(map.containsValue("OtherValue"));
    }

    @Test
    void keySet() {
        // Given
        final WeakIdentityMap<ConstantHashObject, String> map = new WeakIdentityMap<>();
        final ConstantHashObject key1 = new ConstantHashObject();
        final ConstantHashObject key2 = new ConstantHashObject();
        final ConstantHashObject key3 = new ConstantHashObject();
        final ConstantHashObject key4 = new ConstantHashObject();
        map.put(key1, "Value");
        map.put(key2, "Value2");
        map.put(key3, "Value3");
        map.put(key4, "Value4");

        // When
        final Collection<ConstantHashObject> result = map.keySet();

        // Then
        assertFalse(result.isEmpty());
        assertEquals(4, result.size());
        assertTrue(result.contains(key1));
        assertTrue(result.contains(key2));
        assertTrue(result.contains(key3));

        // Test WeakKeySet methods
        final ConstantHashObject[] resultArray = result.toArray(new ConstantHashObject[0]);
        assertEquals(4, resultArray.length);

        assertTrue(result.contains(resultArray[0]));
        assertTrue(result.contains(resultArray[1]));
        assertTrue(result.contains(resultArray[2]));
        assertTrue(result.contains(resultArray[3]));

        final Object[] objectResultArray = result.toArray();
        assertEquals(4, objectResultArray.length);
        assertTrue(result.containsAll(List.of(objectResultArray)));

        result.remove(key1);
        assertEquals(3, result.size());
        assertFalse(result.contains(key1));
        assertFalse(result.containsAll(List.of(objectResultArray)));

        result.removeAll(List.of(key1, key2));
        assertEquals(2, result.size());

        result.clear();
        assertTrue(result.isEmpty());

        final ConstantHashObject testObject = new ConstantHashObject();
        assertTrue(result.add(testObject));
        assertEquals(1, result.size());
        assertFalse(result.add(testObject));
        assertEquals(1, result.size());

        ConstantHashObject tempObject1 = new ConstantHashObject();
        ConstantHashObject tempObject2 = new ConstantHashObject();
        final WeakReference<ConstantHashObject> tempObject1Ref = new WeakReference<>(tempObject1);
        final WeakReference<ConstantHashObject> tempObject2Ref = new WeakReference<>(tempObject2);
        assertTrue(result.addAll(List.of(tempObject1, tempObject2, testObject)));
        assertEquals(3, result.size());

        tempObject1 = null;
        awaitCollected(tempObject1Ref);
        assertEquals(2, result.size());

        assertFalse(result.retainAll(List.of(testObject, tempObject2)));
        tempObject1 = new ConstantHashObject();
        tempObject2 = new ConstantHashObject();
        assertTrue(result.retainAll(List.of(tempObject1, tempObject2)));
    }

    @Test
    void keySet_containsAll_false() {
        final WeakIdentityMap<Object, String> map = new WeakIdentityMap<>();
        map.put(new Object(), "value");
        assertFalse(map.keySet().containsAll(List.of(new Object())));
        assertTrue(map.keySet().containsAll(List.of())); // Empty collection
    }

    @Test
    void keySet_addAll_empty() {
        final WeakIdentityMap<Object, String> map = new WeakIdentityMap<>();
        assertFalse(map.keySet().addAll(List.of()));
    }

    @Test
    void keySet_removeAll_empty() {
        final WeakIdentityMap<Object, String> map = new WeakIdentityMap<>();
        map.put(new Object(), "value");
        assertFalse(map.keySet().removeAll(List.of()));
    }

    @Test
    void keySet_retainAll_empty() {
        final WeakIdentityMap<Object, String> map = new WeakIdentityMap<>();
        map.put(new Object(), "value");
        assertTrue(map.keySet().retainAll(List.of()));
        assertTrue(map.isEmpty());
    }

    @Test
    void keySet_toArray_staleEntry() {
        final WeakIdentityMap<Object, String> map = new WeakIdentityMap<>();
        Object key1 = new Object();
        map.put(key1, "value1");

        final WeakReference<Object> ref = new WeakReference<>(key1);
        key1 = null;
        awaitCollected(ref);

        // At this point the ref is still in innerMap but get() returns null
        // toArray should skip it
        Object[] array = map.keySet().toArray();
        assertEquals(0, array.length);
    }

    @Test
    void keySet_toArray_mixedStaleEntries() {
        final WeakIdentityMap<Object, String> map = new WeakIdentityMap<>();
        Object key1 = new Object();
        Object key2 = new Object();
        map.put(key1, "value1");
        map.put(key2, "value2");

        final WeakReference<Object> ref = new WeakReference<>(key1);
        key1 = null;
        awaitCollected(ref);

        Object[] array = map.keySet().toArray();
        assertEquals(1, array.length);
        assertEquals(key2, array[0]);
    }

    @Test
    void keySet_retainAll_onEmptyMap() {
        final WeakIdentityMap<Object, String> map = new WeakIdentityMap<>();
        assertFalse(map.keySet().retainAll(List.of("anything")));
    }

    @Test
    void keySet_removeAll_notModified() {
        final WeakIdentityMap<Object, String> map = new WeakIdentityMap<>();
        map.put(new Object(), "value");
        assertFalse(map.keySet().removeAll(List.of(new Object())));
    }
    
    @Test
    void keySet_addAll_notModified() {
        final WeakIdentityMap<Object, String> map = new WeakIdentityMap<>();
        Object key = new Object();
        map.put(key, "value");
        assertFalse(map.keySet().addAll(List.of(key)));
    }

    @Test
    void keySet_iterator() {
        // Given
        final WeakIdentityMap<ConstantHashObject, String> map = new WeakIdentityMap<>();
        final ConstantHashObject key1 = new ConstantHashObject();
        final ConstantHashObject key2 = new ConstantHashObject();
        final ConstantHashObject key3 = new ConstantHashObject();
        final ConstantHashObject key4 = new ConstantHashObject();
        map.put(key1, "Value");
        map.put(key2, "Value2");
        map.put(key3, "Value3");
        map.put(key4, "Value4");

        // When
        final Collection<ConstantHashObject> result = map.keySet();

        // Then
        assertFalse(result.isEmpty());
        assertEquals(4, result.size());
        assertTrue(result.contains(key1));
        assertTrue(result.contains(key2));
        assertTrue(result.contains(key3));

        // Test WeakKeySet iterator methods
        final Iterator<ConstantHashObject> iterator1 = result.iterator();

        while (iterator1.hasNext()) {
            final ConstantHashObject next = iterator1.next();
            assertTrue(result.contains(next));
        }
        assertThrows(NoSuchElementException.class, iterator1::next);

        final Iterator<ConstantHashObject> iterator2 = result.iterator();

        while (iterator2.hasNext()) {
            iterator2.next();
            iterator2.remove();
        }

        assertTrue(result.isEmpty());
    }

    @Test
    void values() {
        // Given
        final WeakIdentityMap<Object, String> map = new WeakIdentityMap<>();
        final ConstantHashObject key1 = new ConstantHashObject();
        final String value1 = "Value";
        final ConstantHashObject key2 = new ConstantHashObject();
        final String value2 = "Value2";
        map.put(key1, value1);
        map.put(key2, value2);

        // When
        final Collection<String> result = map.values();

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains(value1));
        assertTrue(result.contains(value2));
    }

    @Test
    void entrySet() {
        // Given
        final WeakIdentityMap<Object, String> map = new WeakIdentityMap<>();
        final ConstantHashObject key1 = new ConstantHashObject();
        final String value1 = "Value";
        final ConstantHashObject key2 = new ConstantHashObject();
        final String value2 = "Value2";
        map.put(key1, value1);
        map.put(key2, value2);

        // When
        final Set<Map.Entry<Object, String>> result = map.entrySet();

        // Then
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(entry -> entry.getKey().equals(key1) && entry.getValue().equals(value1)));
        assertTrue(result.stream().anyMatch(entry -> entry.getKey().equals(key2) && entry.getValue().equals(value2)));
    }

    @Test
    void iterator_remove_withoutNext() {
        // Given
        final WeakIdentityMap<Object, String> map = new WeakIdentityMap<>();
        map.put(new Object(), "value");
        final Iterator<Object> it = map.keySet().iterator();

        // When/Then
        assertThrows(IllegalStateException.class, it::remove);
    }

    @Test
    void identityWeakReference_equals() {
        // Given
        final Object key = new Object();
        final WeakIdentityMap.IdentityWeakReference<Object> ref1 = new WeakIdentityMap.IdentityWeakReference<>(key, null);
        final WeakIdentityMap.IdentityWeakReference<Object> ref2 = new WeakIdentityMap.IdentityWeakReference<>(key, null);
        final WeakIdentityMap.IdentityWeakReference<Object> ref3 = new WeakIdentityMap.IdentityWeakReference<>(new Object(), null);

        // When/Then
        assertEquals(ref1, ref1);
        assertEquals(ref1, ref2);
        assertNotEquals(ref1, ref3);
        assertNotEquals(ref1, new Object());
        assertNotEquals(ref1, null);
    }

    @Test
    void identityLookupWrapper_equals() {
        // Given
        final Object key = new Object();
        final WeakIdentityMap.IdentityLookupWrapper wrapper = new WeakIdentityMap.IdentityLookupWrapper(key);
        final WeakIdentityMap.IdentityWeakReference<Object> ref = new WeakIdentityMap.IdentityWeakReference<>(key, null);

        // When/Then
        assertTrue(wrapper.equals(ref));
        assertFalse(wrapper.equals(new Object()));
        assertFalse(wrapper.equals(null));
    }

    @Test
    void iterator_staleEntry() {
        final WeakIdentityMap<Object, String> map = new WeakIdentityMap<>();
        Object key1 = new Object();
        map.put(key1, "value1");

        Iterator<Object> it = map.keySet().iterator();

        // key1 is now eligible for GC
        final WeakReference<Object> ref = new WeakReference<>(key1);
        key1 = null;
        awaitCollected(ref);

        // it.hasNext() should trigger stale entry removal and return false
        assertFalse(it.hasNext());
        assertTrue(map.isEmpty());
    }

    private static class ConstantHashObject {
        @Override
        public int hashCode() {
            return 42;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof ConstantHashObject;
        }

        @Override
        public String toString() {
            return "ConstantHashObject@" + System.identityHashCode(this);
        }
    }

    private static void awaitCollected(WeakReference<?> ref) {
        // Wait until the referent is actually cleared.
        while (ref.get() != null) {
            System.gc();
            LockSupport.parkNanos(Duration.ofMillis(10).toNanos());
        }
    }
}