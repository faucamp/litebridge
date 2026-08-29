package org.litebridge.orm.api.select.impl;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.api.select.LimitClauseTerminal;
import org.litebridge.orm.api.select.SelectTerminal;
import org.litebridge.orm.engine.ast.LimitNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngineTerminal;

public class LimitClauseTerminalImpl<DTO>
        extends DelegatingSelectTerminal<DTO>
        implements LimitClauseTerminal<DTO> {

    private final @Nullable Integer limit;

    public LimitClauseTerminalImpl(final QueryNode node,
                                   final SelectEngineTerminal selectEngineTerminal,
                                   final LitebridgeContext litebridgeContext) {
        super(node, selectEngineTerminal, litebridgeContext);
        this.limit = null;
    }

    public LimitClauseTerminalImpl(final Integer limit,
                                   final QueryNode node,
                                   final SelectEngineTerminal selectEngineTerminal,
                                   final LitebridgeContext litebridgeContext) {
        super(node, selectEngineTerminal, litebridgeContext);
        this.limit = limit;
        this.pendingNode = () -> new LimitNode(node, limit, null);
    }

    @Override
    public SelectTerminal<DTO> offset(final int offset) {
        pendingNode = null;
        node = new LimitNode(node, limit, offset);
        return this;
    }
}
