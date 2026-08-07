package org.litebridge.orm.api.select.impl;

import org.litebridge.orm.api.select.LimitClauseTerminal;
import org.litebridge.orm.api.select.OrderByClauseTerminal;
import org.litebridge.orm.api.select.ast.LimitNode;
import org.litebridge.orm.api.select.model.SelectSpec;

import java.util.Optional;

public class OrderByClauseTerminalImpl<DTO, SSP extends SelectSpec>
        extends LimitClauseTerminalImpl<DTO, SSP>
        implements OrderByClauseTerminal<DTO> {

    public OrderByClauseTerminalImpl(final AbstractSelector<DTO, SSP> delegate) {
        super(delegate);
    }

    @Override
    public LimitClauseTerminal<DTO> limit(final int limit) {
        return new LimitClauseTerminalImpl<>(delegate.withNode(new LimitNode(delegate.node(), Optional.of(limit), Optional.empty())));
    }
}
