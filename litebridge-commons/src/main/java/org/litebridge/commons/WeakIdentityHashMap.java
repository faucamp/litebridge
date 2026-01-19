package org.litebridge.commons;

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

public final class WeakIdentityHashMap<K, V> implements Map<K, V> {

    private final ReferenceQueue<K> queue = new ReferenceQueue<>();
    private final Map<IdentityWeakReference<K>, V> innerMap = new HashMap<>();

    private void expungeStaleEntries() {
        IdentityWeakReference<?> ref;

        while ((ref = (IdentityWeakReference<?>) queue.poll()) != null) {
            innerMap.remove(ref);
        }
    }

    @Override
    public V put(K key, V value) {
        expungeStaleEntries();
        return innerMap.put(new IdentityWeakReference<>(key, queue), value);
    }

    @Override
    public V get(Object key) {
        expungeStaleEntries();
        return innerMap.get(new IdentityLookupWrapper(key));
    }

    @Override
    public V remove(Object key) {
        expungeStaleEntries();
        return innerMap.remove(new IdentityLookupWrapper(key));
    }

    @Override
    public boolean containsKey(Object key) {
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
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public boolean containsValue(Object value) {
        expungeStaleEntries();
        return innerMap.containsValue(value);
    }

    @Override
    public Set<K> keySet() {
        return new WeakKeySet();
    }

    @Override
    public Collection<V> values() {
        expungeStaleEntries();
        return innerMap.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return keySet().stream().map(key -> Map.entry(key, get(key)))
                .collect(Collectors.toSet());
    }

    private final class WeakKeySet implements Set<K> {
        @Override
        public int size() {
            return WeakIdentityHashMap.this.size();
        }

        @Override
        public boolean isEmpty() {
            return WeakIdentityHashMap.this.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return WeakIdentityHashMap.this.containsKey(o);
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
        public boolean add(K e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object o) {
            return WeakIdentityHashMap.this.remove(o) != null;
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            for (Object e : c) {
                if (!contains(e)) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public boolean addAll(Collection<? extends K> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            boolean modified = false;

            for (Object e : c) {
                modified |= remove(e);
            }

            return modified;
        }

        @Override
        public void clear() {
            WeakIdentityHashMap.this.clear();
        }
    }

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