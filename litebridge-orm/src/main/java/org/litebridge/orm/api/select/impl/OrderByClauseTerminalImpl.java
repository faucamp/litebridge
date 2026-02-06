package org.litebridge.orm.api.select.impl;

import org.litebridge.orm.api.select.LimitClauseTerminal;
import org.litebridge.orm.api.select.OrderByClauseTerminal;
import org.litebridge.orm.api.select.model.SelectSpec;

public class OrderByClauseTerminalImpl<DTO, SSP extends SelectSpec>
        extends LimitClauseTerminalImpl<DTO, SSP>
        implements OrderByClauseTerminal<DTO> {

    public OrderByClauseTerminalImpl(final AbstractSelector<DTO, SSP> delegate) {
        super(delegate);
    }

    @Override
    public LimitClauseTerminal<DTO> limit(final int limit) {
        selectSpec.ensureLimit().setLimit(limit);
        return new LimitClauseTerminalImpl<>(delegate);
    }
}
