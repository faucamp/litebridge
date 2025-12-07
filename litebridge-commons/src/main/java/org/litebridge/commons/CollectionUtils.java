package org.litebridge.commons;

import jakarta.annotation.Nullable;

import java.util.Collection;
import java.util.Map;

public final class CollectionUtils {

    private CollectionUtils() {
    }

    /**
     * Checks if the provided array is null or empty.
     *
     * @param array the array to check; may be null
     * @return true if the array is null or empty; false otherwise
     */
    public static boolean isEmpty(@Nullable final Object[] array) {
        return array == null || array.length == 0;
    }

    /**
     * Checks if the provided iterable is null or empty.
     *
     * @param iterable the iterable to check; may be null
     * @return true if the iterable is null or empty; false otherwise
     */
    public static boolean isEmpty(@Nullable final Iterable<?> iterable) {
        return iterable == null || !iterable.iterator().hasNext();
    }

    /**
     * Checks if the provided collection is null or empty.
     *
     * @param collection the collection to check; may be null
     * @return true if the collection is null or empty; false otherwise
     */
    public static boolean isEmpty(@Nullable final Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * Checks if the provided map is null or empty.
     *
     * @param map the map to check; may be null
     * @return true if the map is null or empty; false otherwise
     */
    public static boolean isEmpty(@Nullable final Map<?, ?> map) {
        return map == null || map.isEmpty();
    }
}
