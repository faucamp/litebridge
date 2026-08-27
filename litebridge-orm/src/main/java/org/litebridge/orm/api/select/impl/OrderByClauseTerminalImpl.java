package org.litebridge.orm.api.select.impl;

import org.litebridge.orm.api.select.LimitClauseTerminal;
import org.litebridge.orm.api.select.OrderByClauseTerminal;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngineTerminal;

public class OrderByClauseTerminalImpl<DTO>
        extends LimitClauseTerminalImpl<DTO>
        implements OrderByClauseTerminal<DTO> {

    public OrderByClauseTerminalImpl(final QueryNode node, final SelectEngineTerminal selectEngineTerminal, final LitebridgeContext litebridgeContext) {
        super(node, selectEngineTerminal, litebridgeContext);
    }

    @Override
    public LimitClauseTerminal<DTO> limit(final int limit) {
        return new LimitClauseTerminalImpl<>(limit, node, selectEngineTerminal, litebridgeContext);
    }
}
