package org.litebridge.orm.api.select.impl;

import org.litebridge.orm.api.select.LimitClauseTerminal;
import org.litebridge.orm.api.select.SelectTerminal;
import org.litebridge.orm.api.select.ast.LimitNode;
import org.litebridge.orm.api.select.model.SelectSpec;

import java.util.Optional;

public class LimitClauseTerminalImpl<DTO, SSP extends SelectSpec>
        extends DelegatingSelector<DTO, SSP>
        implements LimitClauseTerminal<DTO> {

    protected final SSP selectSpec;

    public LimitClauseTerminalImpl(final AbstractSelector<DTO, SSP> delegate) {
        super(delegate);
        this.selectSpec = delegate.selectSpec();
    }

    @Override
    public SelectTerminal<DTO> offset(final int offset) {
        return delegate.withNode(new LimitNode(delegate.node(), Optional.empty(), Optional.of(offset)));
    }
}
