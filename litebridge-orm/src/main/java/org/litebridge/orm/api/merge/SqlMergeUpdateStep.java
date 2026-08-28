package org.litebridge.orm.api.merge;

import org.litebridge.db.spi.Row;
import org.litebridge.orm.api.delete.DeleteTerminalInspector;
import org.litebridge.orm.api.delete.SqlDeleteStart;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.update.SqlUpdateStart;
import org.litebridge.orm.api.update.UpdateQuery;
import org.litebridge.orm.api.update.UpdateQueryInspector;
import org.litebridge.orm.engine.LitebridgeContext;

import java.util.function.Function;

public final class SqlMergeUpdateStep extends MergeUpdateStep<Row> {

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
        final SqlDeleteStart sqlDeleteStart = new SqlDeleteStart(table, litebridgeContext);
        //TODO: where condition
        final QueryNode deleteNode = DeleteTerminalInspector.getNode(sqlDeleteStart);
        return new MergeTerminal(deleteNode);
    }
}
