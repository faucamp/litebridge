package org.litebridge.orm;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public final class WeakRefSet<T> {

    private final Set<T> set;

    public WeakRefSet() {
        this.set = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<T, Boolean>()));
    }

    public boolean add(T element) {
        return set.add(element);
    }

    public boolean remove(T element) {
        return set.remove(element);
    }

    public boolean contains(T element) {
        return set.contains(element);
    }

    public int size() {
        return set.size();
    }
}
