package org.litebridge.orm.api.merge;

import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.Table;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.QueryCompiler;
import org.litebridge.orm.engine.QueryPlanCache;

import java.util.function.Function;

public class SqlMerger {

    private final LitebridgeContext litebridgeContext;

    public SqlMerger(final LitebridgeContext litebridgeContext) {
        this.litebridgeContext = litebridgeContext;
    }

    public void mergeInto(final String tableName, final Function<SqlMergeUsingStep, MergeTerminal> merge) {
        final SqlMergeUsingStep mergeUsingStep = new SqlMergeUsingStep(new Table(tableName), litebridgeContext);
        final MergeTerminal mergeTerminal = merge.apply(mergeUsingStep);
        final QueryNode node = mergeTerminal.node();
        final int nodeHash = node.hashCode();
        final QueryPlanCache.CachedOperation cachedOperation = litebridgeContext.queryPlanCache().get(nodeHash);

        if (cachedOperation != null) {
            throw new UnsupportedOperationException("Not yet implemented");
        } else {
            final QueryCompiler queryCompiler = litebridgeContext.createQueryCompiler();
            queryCompiler.compile(node);
            final DatabaseProvider databaseProvider = litebridgeContext.databaseProvider();
            databaseProvider.toSql()
        }
    }

}
