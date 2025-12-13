package org.litebridge.commons;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
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

    /**
     * Ensures that the provided collection is not null or empty. If the collection is null or empty,
     * an {@link IllegalArgumentException} is thrown with the given message.
     *
     * @param collection the collection to validate; may be null
     * @param message    the exception message to use if the validation fails
     * @return the validated non-null and non-empty collection
     * @throws IllegalArgumentException if the collection is null or empty
     */
    public static <T extends Collection<?>> T requireNonEmpty(@Nullable final T collection, final String message) {
        if (isEmpty(collection)) {
            throw new IllegalArgumentException(message);
        }

        return collection;
    }

    /**
     * Ensures that the provided map is not null or empty. If the map is null or empty,
     * an {@link IllegalArgumentException} is thrown with the given message.
     *
     * @param map     the map to validate; may be null
     * @param message the exception message to use if the validation fails
     * @return the validated non-null and non-empty map
     * @throws IllegalArgumentException if the map is null or empty
     */
    public static <T extends Map<?, ?>> T requireNonEmpty(@Nullable final T map, final String message) {
        if (isEmpty(map)) {
            throw new IllegalArgumentException(message);
        }

        return map;
    }

    /**
     * Converts the given array to a {@link List}. If the array is null or empty,
     * an immutable empty list is returned. Otherwise, a list containing the
     * elements of the array is returned.
     *
     * @param array the array to convert to a list; may be null
     * @return a non-null list containing the elements of the array, or an immutable empty list if the array is null or empty
     */
    public static @Nonnull <T> List<T> toList(final @Nullable T... array) {
        final List<T> list;

        if (array == null || array.length == 0) {
            list = Collections.emptyList();
        } else {
            list = List.of(array);
        }

        return list;
    }
}
