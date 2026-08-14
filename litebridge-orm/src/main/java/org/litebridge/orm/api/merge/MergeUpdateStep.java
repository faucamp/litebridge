package org.litebridge.orm.api.merge;

import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.orm.api.select.ast.DeleteNode;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.ast.UsingNode;
import org.litebridge.orm.engine.LitebridgeContext;

public abstract sealed class MergeUpdateStep<DTO> permits DtoMergeUpdateStep, SqlMergeUpdateStep {

    protected final Table table;
    protected final QueryNode node;
    protected final LitebridgeContext litebridgeContext;

    public MergeUpdateStep(final Table table, final QueryNode node, final LitebridgeContext litebridgeContext) {
        this.table = table;
        this.node = node;
        this.litebridgeContext = litebridgeContext;
    }

    public MergeTerminal delete() {
        final DeleteNode delete = new DeleteNode(null, table);
        return new MergeTerminal(new DeleteNode(node, table));
    }
}
