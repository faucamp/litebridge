package org.litebridge.orm.api.select.impl;

import org.litebridge.orm.api.select.LimitClauseTerminal;
import org.litebridge.orm.api.select.OrderByClauseTerminal;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngineTerminal;
import org.litebridge.orm.engine.ast.QueryNode;

/**
 * Implementation of {@link OrderByClauseTerminal} for ORDER BY terminal clauses.
 *
 * @param <DTO> the mapped DTO/entity type or row type
 */
public class OrderByClauseTerminalImpl<DTO>
        extends LimitClauseTerminalImpl<DTO>
        implements OrderByClauseTerminal<DTO> {

    /**
     * Creates a new {@code OrderByClauseTerminalImpl} instance.
     *
     * @param node                 the query node
     * @param selectEngineTerminal the terminal select engine
     * @param litebridgeContext    the Litebridge context
     */
    public OrderByClauseTerminalImpl(final QueryNode node,
                                     final SelectEngineTerminal selectEngineTerminal,
                                     final LitebridgeContext litebridgeContext) {
        super(node, selectEngineTerminal, litebridgeContext);
    }

    @Override
    public LimitClauseTerminal<DTO> limit(final int limit) {
        return new LimitClauseTerminalImpl<>(limit, node, selectEngineTerminal, litebridgeContext);
    }
}
