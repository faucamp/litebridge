package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe cache for storing compiled DTO {@link DtoMapper.MappingPlan} instances.
 */
public final class MappingPlanCache {

    private final Map<MappingPlanKey, DtoMapper.MappingPlan> cache = new ConcurrentHashMap<>();

    /**
     * Retrieves a cached mapping plan for the specified key.
     *
     * @param key the mapping plan cache key
     * @return the cached mapping plan, or {@code null} if not found
     */
    public DtoMapper.@Nullable MappingPlan get(final MappingPlanKey key) {
        return cache.get(key);
    }

    /**
     * Stores a mapping plan in the cache.
     *
     * @param key  the mapping plan cache key
     * @param plan the mapping plan to cache
     */
    public void put(final MappingPlanKey key, final DtoMapper.MappingPlan plan) {
        cache.put(key, plan);
    }

    /**
     * Returns the current cache size.
     *
     * @return the number of cached mapping plans
     */
    public int size() {
        return cache.size();
    }

    /**
     * Clears the cache.
     */
    public void clear() {
        cache.clear();
    }

    /**
     * Returns the underlying cache map.
     *
     * @return the underlying cache map
     */
    public Map<MappingPlanKey, DtoMapper.MappingPlan> cache() {
        return cache;
    }
}
