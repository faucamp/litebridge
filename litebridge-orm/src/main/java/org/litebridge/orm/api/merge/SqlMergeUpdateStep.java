package org.litebridge.orm.api.merge;

import org.litebridge.orm.api.update.SqlUpdateStart;
import org.litebridge.orm.api.update.UpdateQuery;
import org.litebridge.orm.api.update.UpdateQueryInspector;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.DeleteNode;
import org.litebridge.orm.engine.ast.QueryNode;

import java.util.function.Function;

public final class SqlMergeUpdateStep extends MergeUpdateStep {

    private final String table;

    public SqlMergeUpdateStep(final String table, final QueryNode node, final LitebridgeContext litebridgeContext) {
        super(node, litebridgeContext);
        this.table = table;
    }

    public MergeTerminal update(final Function<SqlUpdateStart, UpdateQuery> update) {
        final SqlUpdateStart sqlUpdateStart = new SqlUpdateStart(table, litebridgeContext);
        final UpdateQuery updateQuery = update.apply(sqlUpdateStart);
        final QueryNode setNode = UpdateQueryInspector.getNode(updateQuery);
        return new MergeTerminal(setNode);
    }

    public MergeTerminal delete() {
        final QueryNode deleteNode = new DeleteNode(null, table, null);
        //TODO: where condition
        return new MergeTerminal(deleteNode);
    }
}
