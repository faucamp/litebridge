package org.litebridge.orm.engine;

import org.litebridge.db.spi.query.Select;
import org.litebridge.db.spi.sql.PreparedSql;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caches SQL execution plans based on the structural fingerprint of a {@link Select} operation.
 */
public final class QueryPlanCache {

    private final Map<Select, PreparedSql> cache = new ConcurrentHashMap<>();

    /**
     * Retrieves a cached execution plan for the given select operation.
     *
     * @param select the select operation
     * @return the cached prepared SQL, or {@code null} if not found
     */
    public PreparedSql get(final Select select) {
        return cache.get(select);
    }

    /**
     * Stores an execution plan in the cache.
     *
     * @param select      the select operation (used as key)
     * @param preparedSql the generated execution plan
     */
    public void put(final Select select, final PreparedSql preparedSql) {
        cache.put(select, preparedSql);
    }

    /**
     * Clears the cache.
     */
    public void clear() {
        cache.clear();
    }
}
