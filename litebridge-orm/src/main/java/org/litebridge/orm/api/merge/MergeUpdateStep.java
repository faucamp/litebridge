package org.litebridge.orm.api.merge;

import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.LitebridgeContext;

public abstract sealed class MergeUpdateStep<DTO> permits DtoMergeUpdateStep, SqlMergeUpdateStep {

    protected final QueryNode node;
    protected final LitebridgeContext litebridgeContext;

    public MergeUpdateStep(final QueryNode node, final LitebridgeContext litebridgeContext) {
        this.node = node;
        this.litebridgeContext = litebridgeContext;
    }

    public abstract MergeTerminal delete();
}
