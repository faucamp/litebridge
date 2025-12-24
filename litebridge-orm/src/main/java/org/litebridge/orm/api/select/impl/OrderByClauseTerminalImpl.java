package org.litebridge.orm.api.select.impl;

import org.litebridge.orm.api.select.LimitClauseTerminal;
import org.litebridge.orm.api.select.OrderByClauseTerminal;

public class OrderByClauseTerminalImpl<DTO>
        extends LimitClauseTerminalImpl<DTO>
        implements OrderByClauseTerminal<DTO> {

    public OrderByClauseTerminalImpl(final AbstractSelector<DTO> delegate) {
        super(delegate);
    }

    @Override
    public LimitClauseTerminal<DTO> limit(final int limit) {
        selectSpec.ensureLimit().setLimit(limit);
        return new LimitClauseTerminalImpl<>(delegate);
    }
}
