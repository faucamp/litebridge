package org.litebridge.commons;

import org.jspecify.annotations.Nullable;

import java.util.Map;

public final class MapUtils {

    private MapUtils() {
    }

    /**
     * Checks if the specified key is present in the given map.
     *
     * @param key the key to check for in the map
     * @param map the map in which to look for the key; can be {@code null}.
     * @return {@code true} if the the map is not {@code null} and the key is present, otherwise {@code false}.
     */
    public static boolean containsKey(final Object key, final @Nullable Map<?, ?> map) {
        if (map == null) {
            return false;
        }

        return map.containsKey(key);
    }
}
