package org.litebridge.orm;

import org.litebridge.orm.engine.QueryPlanCache;

/**
 * Inspector for {@link LitebridgeCore} instances, providing access to internal components.
 */
public final class LitebridgeInspector {

    private LitebridgeInspector() {
    }

    /**
     * Retrieves the {@link QueryPlanCache} instance associated with the specified {@link LitebridgeCore}.
     *
     * @param litebridge the Litebridge instance
     * @return the query plan cache
     */
    public static QueryPlanCache getQueryPlanCache(final LitebridgeCore litebridge) {
        return litebridge.queryPlanCache();
    }
}
