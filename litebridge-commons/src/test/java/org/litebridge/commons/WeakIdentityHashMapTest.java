package org.litebridge.commons;

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

class WeakIdentityHashMapTest {

    /**
     * Test that put adds a key-value pair into the map and allows retrieval of that value using the key.
     */
    @Test
    void put() {
        // Given
        final WeakIdentityHashMap<Object, String> map = new WeakIdentityHashMap<>();
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
        final WeakIdentityHashMap<Object, String> map = new WeakIdentityHashMap<>();
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
        final WeakIdentityHashMap<Object, String> map = new WeakIdentityHashMap<>();
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
        final WeakIdentityHashMap<Object, String> map = new WeakIdentityHashMap<>();
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
        final WeakIdentityHashMap<Object, String> map = new WeakIdentityHashMap<>();
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
        final WeakIdentityHashMap<Object, String> map = new WeakIdentityHashMap<>();
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
        final WeakIdentityHashMap<Object, String> map = new WeakIdentityHashMap<>();
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
        final WeakIdentityHashMap<Object, String> map = new WeakIdentityHashMap<>();
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
        final WeakIdentityHashMap<Object, String> map = new WeakIdentityHashMap<>();
        final Object key = new Object();
        final String value = "Value";
        map.put(key, value);

        // When/Then
        assertEquals(1, map.size());
    }

    @Test
    void isEmpty() {
        // Given
        final WeakIdentityHashMap<Object, String> map = new WeakIdentityHashMap<>();

        // When/Then
        assertTrue(map.isEmpty());
        map.put(new Object(), "Value");
        assertFalse(map.isEmpty());
    }

    @Test
    void clear() {
        // Given
        final WeakIdentityHashMap<Object, String> map = new WeakIdentityHashMap<>();
        final Object key = new Object();
        final String value = "Value";
        map.put(key, value);

        // When
        map.clear();

        // Then
        assertTrue(map.isEmpty());
    }

    @Test
    void putAll() {
        // Given
        final WeakIdentityHashMap<Object, String> map = new WeakIdentityHashMap<>();
        final WeakIdentityHashMap<Object, String> otherMap = new WeakIdentityHashMap<>();
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
        final WeakIdentityHashMap<Object, String> map = new WeakIdentityHashMap<>();
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
        final WeakIdentityHashMap<ConstantHashObject, String> map = new WeakIdentityHashMap<>();
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

        assertThrows(UnsupportedOperationException.class, () -> result.add(new ConstantHashObject()));
        assertThrows(UnsupportedOperationException.class, () -> result.addAll(List.of()));
        assertThrows(UnsupportedOperationException.class, () -> result.retainAll(List.of()));

        result.remove(key1);
        assertEquals(3, result.size());
        assertFalse(result.contains(key1));
        assertFalse(result.containsAll(List.of(objectResultArray)));

        result.removeAll(List.of(key1, key2));
        assertEquals(2, result.size());

        result.clear();
        assertTrue(result.isEmpty());
    }

    @Test
    void keySet_iterator() {
        // Given
        final WeakIdentityHashMap<ConstantHashObject, String> map = new WeakIdentityHashMap<>();
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
        final WeakIdentityHashMap<Object, String> map = new WeakIdentityHashMap<>();
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
        final WeakIdentityHashMap<Object, String> map = new WeakIdentityHashMap<>();
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

    private static class ConstantHashObject {
        @Override
        public int hashCode() {
            return 42;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof ConstantHashObject;
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