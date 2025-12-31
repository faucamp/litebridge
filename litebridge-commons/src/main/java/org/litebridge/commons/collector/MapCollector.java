package org.litebridge.commons.collector;

import java.util.LinkedHashMap;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public final class MapCollector {

    private MapCollector() {
    }

    public static <T, K, U>
    Collector<T, ?, LinkedHashMap<K, U>> toLinkedHashMap(Function<? super T, ? extends K> keyMapper,
                                                         Function<? super T, ? extends U> valueMapper) {
        return Collectors.toMap(keyMapper,
                valueMapper,
                (oldValue, newValue) -> newValue,
                LinkedHashMap::new);
    }
}
