package org.litebridge.orm.api.merge;

import org.litebridge.orm.api.delete.DeleteTerminal;
import org.litebridge.orm.api.delete.SqlDeleteStart;
import org.litebridge.orm.api.update.SqlUpdateStart;
import org.litebridge.orm.api.update.UpdateQuery;
import org.litebridge.orm.engine.DeleteEngine;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.UpdateEngine;
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
        final QueryNode terminalNode = UpdateEngine.createUpdateNodeChain(table, update, litebridgeContext);
        return new MergeTerminal(terminalNode);
    }

    public MergeTerminal delete(final Function<SqlDeleteStart, DeleteTerminal> delete) {
        final QueryNode deleteNode = DeleteEngine.createDeleteNodeChain(table, delete, litebridgeContext);
        return new MergeTerminal(deleteNode);
    }

    public MergeTerminal delete() {
        final DeleteNode deleteNode = new DeleteNode(null, table, null);
        return new MergeTerminal(deleteNode);
    }
}
