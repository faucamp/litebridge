package org.litebridge.commons.type;

import java.util.AbstractSet;
import java.util.Iterator;

/**
 * A {@code Set} implementation that uses identity comparison for object equality,
 * and holds its elements weakly, meaning elements are eligible for garbage
 * collection if no strong references to them exist outside this set.
 * <p>
 * This class is backed by a {@link WeakIdentityMap} with the keys representing
 * the elements of the set and values serving as placeholders.
 *
 * @param <E> the type of elements maintained by this set
 */
public final class WeakIdentitySet<E> extends AbstractSet<E> {

    private final WeakIdentityMap<E, Boolean> map;

    public WeakIdentitySet() {
        map = new WeakIdentityMap<>();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public Iterator<E> iterator() {
        return map.keySet().iterator();
    }

    @Override
    public boolean add(E e) {
        return map.put(e, Boolean.TRUE) == null;
    }

    @Override
    public boolean contains(final Object o) {
        return map.containsKey(o);
    }

    @Override
    public boolean remove(final Object o) {
        return map.remove(o) != null;
    }
}
