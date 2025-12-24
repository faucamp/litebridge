package org.litebridge.orm.api.select.impl;

import org.litebridge.orm.api.select.LimitClauseTerminal;
import org.litebridge.orm.api.select.SelectTerminal;
import org.litebridge.orm.api.select.model.SelectSpec;

public class LimitClauseTerminalImpl<DTO>
        extends DelegatingSelector<DTO>
        implements LimitClauseTerminal<DTO> {

    protected final SelectSpec selectSpec;

    public LimitClauseTerminalImpl(final AbstractSelector<DTO> delegate) {
        super(delegate);
        selectSpec = delegate.selectSpec();
    }

    @Override
    public SelectTerminal<DTO> offset(final int offset) {
        selectSpec().ensureLimit().setOffset(offset);
        return this;
    }

    protected final SelectSpec selectSpec() {
        return selectSpec;
    }
}
