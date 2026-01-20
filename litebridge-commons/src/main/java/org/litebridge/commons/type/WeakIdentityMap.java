package org.litebridge.commons.type;

import org.jspecify.annotations.Nullable;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A Map implementation that uses weak references for keys and compares keys using identity comparison (==).
 * This implementation automatically removes entries when their keys are no longer strongly reachable.
 * <p>
 * This is useful for caching scenarios where:
 * - Keys should be automatically removed when they're no longer referenced elsewhere
 * - Key equality should be based on identity rather than equals()
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 * @see WeakIdentitySet
 */
public final class WeakIdentityMap<K, V> implements Map<K, V> {

    private final ReferenceQueue<K> queue = new ReferenceQueue<>();
    private final Map<IdentityWeakReference<K>, @Nullable V> innerMap = new HashMap<>();

    /**
     * Removes any stale entries whose keys have been garbage collected.
     * This method is called automatically before most map operations.
     */
    private void expungeStaleEntries() {
        IdentityWeakReference<?> ref;

        while ((ref = (IdentityWeakReference<?>) queue.poll()) != null) {
            innerMap.remove(ref);
        }
    }

    @Override
    public V put(final K key, final @Nullable V value) {
        expungeStaleEntries();
        return innerMap.put(new IdentityWeakReference<>(key, queue), value);
    }

    @Override
    public @Nullable V get(final Object key) {
        expungeStaleEntries();
        return innerMap.get(new IdentityLookupWrapper(key));
    }

    @Override
    public @Nullable V remove(final Object key) {
        expungeStaleEntries();
        return innerMap.remove(new IdentityLookupWrapper(key));
    }

    @Override
    public boolean containsKey(final Object key) {
        expungeStaleEntries();
        return innerMap.containsKey(new IdentityLookupWrapper(key));
    }

    @Override
    public int size() {
        expungeStaleEntries();
        return innerMap.size();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public void clear() {
        innerMap.clear();
        // Clear the queue
        while (queue.poll() != null) ;
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> m) {
        for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public boolean containsValue(final Object value) {
        expungeStaleEntries();
        return innerMap.containsValue(value);
    }

    @Override
    public Set<K> keySet() {
        return new WeakKeySet();
    }

    @Override
    public Collection<@Nullable V> values() {
        expungeStaleEntries();
        return innerMap.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return keySet().stream().map(key -> Map.entry(key, get(key)))
                .collect(Collectors.toSet());
    }

    /**
     * A Set view of the keys contained in this map.
     * The set is backed by the map, so changes to the map are reflected in the set, and vice-versa.
     */
    private final class WeakKeySet implements Set<K> {

        @Override
        public int size() {
            return WeakIdentityMap.this.size();
        }

        @Override
        public boolean isEmpty() {
            return WeakIdentityMap.this.isEmpty();
        }

        @Override
        public boolean contains(final Object o) {
            return WeakIdentityMap.this.containsKey(o);
        }

        @Override
        public Iterator<K> iterator() {
            expungeStaleEntries();
            return new Iterator<K>() {
                private final Iterator<IdentityWeakReference<K>> it = innerMap.keySet().iterator();

                private K nextKey;
                private boolean nextReady = false;
                private boolean canRemove = false;

                private void prepareNext() {
                    if (nextReady) {
                        return;
                    }

                    while (it.hasNext()) {
                        IdentityWeakReference<K> ref = it.next();
                        K key = ref.get();

                        if (key != null) {
                            nextKey = key;
                            nextReady = true;
                            return;
                        }

                        // stale entry: backing iterator just returned it, so remove() is legal here
                        it.remove();
                    }

                    nextKey = null;
                    nextReady = true;
                }

                @Override
                public boolean hasNext() {
                    prepareNext();
                    return nextKey != null;
                }

                @Override
                public K next() {
                    prepareNext();
                    if (nextKey == null) {
                        throw new NoSuchElementException();
                    }

                    K result = nextKey;

                    // Consume cached next
                    nextReady = false;
                    nextKey = null;

                    // We have returned the element corresponding to backing it.next() that occurred in prepareNext()
                    canRemove = true;
                    return result;
                }

                @Override
                public void remove() {
                    if (!canRemove) {
                        throw new IllegalStateException();
                    }
                    it.remove();
                    canRemove = false;
                }
            };
        }

        @Override
        public Object[] toArray() {
            return toArrayImpl().toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return toArrayImpl().toArray(a);
        }

        private List<K> toArrayImpl() {
            expungeStaleEntries();
            final List<K> list = new ArrayList<>();

            for (IdentityWeakReference<K> ref : innerMap.keySet()) {
                K key = ref.get();

                if (key != null) {
                    list.add(key);
                }
            }

            return list;
        }

        @Override
        public boolean add(final K e) {
            boolean modified = !WeakIdentityMap.this.containsKey(e);
            WeakIdentityMap.this.put(e, null);
            return modified;
        }

        @Override
        public boolean remove(final Object o) {
            return WeakIdentityMap.this.remove(o) != null;
        }

        @Override
        public boolean containsAll(final Collection<?> c) {
            for (Object e : c) {
                if (!contains(e)) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public boolean addAll(final Collection<? extends K> c) {
            boolean modified = false;

            for (K e : c) {
                modified |= add(e);
            }

            return modified;
        }

        @Override
        public boolean retainAll(final Collection<?> c) {
            expungeStaleEntries();
            boolean modified = false;
            final Iterator<K> it = iterator();

            while (it.hasNext()) {
                final Object next = it.next();

                if (c.stream().noneMatch(e -> e == next)) {
                    it.remove();
                    modified = true;
                }
            }

            return modified;
        }

        @Override
        public boolean removeAll(final Collection<?> c) {
            boolean modified = false;

            for (Object e : c) {
                modified |= remove(e);
            }

            return modified;
        }

        @Override
        public void clear() {
            WeakIdentityMap.this.clear();
        }
    }

    /**
     * A WeakReference that uses identity-based equality and hashing.
     * This class maintains the identity hash code of the referent even after it has been garbage collected.
     *
     * @param <T> the type of the referent
     */
    static final class IdentityWeakReference<T> extends WeakReference<T> {
        private final int hash;

        IdentityWeakReference(final T referent, final ReferenceQueue<T> q) {
            super(referent, q);
            this.hash = System.identityHashCode(referent);
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof IdentityWeakReference<?> other)) return false;
            return this.get() == other.get();
        }
    }

    /**
     * A wrapper class used for looking up entries in the map.
     * This class uses identity-based equality and hashing to match IdentityWeakReference behavior.
     */
    static final class IdentityLookupWrapper {
        private final Object obj;
        private final int hash;

        IdentityLookupWrapper(final Object obj) {
            this.obj = obj;
            this.hash = System.identityHashCode(obj);
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(final Object other) {
            if (other instanceof IdentityWeakReference<?> ref) {
                return ref.get() == this.obj;
            }

            return false;
        }
    }
}