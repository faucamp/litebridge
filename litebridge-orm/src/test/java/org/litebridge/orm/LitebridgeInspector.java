package org.litebridge.orm;

import org.litebridge.orm.engine.QueryPlanCache;

public final class LitebridgeInspector {

    private LitebridgeInspector() {
    }

    public static QueryPlanCache getQueryPlanCache(final LitebridgeCore litebridge) {
        return litebridge.queryPlanCache();
    }
}
