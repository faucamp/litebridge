package org.litebridge.commons.collector;

import java.util.LinkedHashMap;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Utility class for Collectors providing functionality to create {@link LinkedHashMap} instances.
 * <p>
 * This class contains static methods for collecting elements of a Stream into a
 * {@code LinkedHashMap}. It uses user-provided mapping functions to determine
 * both the keys and values of the resulting map.
 * <p>
 * The class is final and contains a private constructor to prevent instantiation,
 * as it is intended to be used solely as a utility class.
 */
public final class MapCollector {

    private MapCollector() {
    }

    /**
     * Returns a {@link Collector} that accumulates elements into a {@link LinkedHashMap}.
     *
     * @param keyMapper   a mapping function to produce keys.
     * @param valueMapper a mapping function to produce values.
     * @param <T>         the type of the input elements.
     * @param <K>         the output type of the key mapping function.
     * @param <U>         the output type of the value mapping function.
     * @return a {@code Collector} which collects elements into a {@code LinkedHashMap} in insertion order.
     */
    public static <T, K, U>
    Collector<T, ?, LinkedHashMap<K, U>> toLinkedHashMap(Function<? super T, ? extends K> keyMapper,
                                                         Function<? super T, ? extends U> valueMapper) {
        return Collectors.toMap(keyMapper,
                valueMapper,
                (oldValue, newValue) -> newValue,
                LinkedHashMap::new);
    }
}
