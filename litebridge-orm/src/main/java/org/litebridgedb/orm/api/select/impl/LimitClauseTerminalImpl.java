package org.litebridgedb.orm.api.select.impl;

import org.litebridgedb.orm.api.select.LimitClauseTerminal;
import org.litebridgedb.orm.api.select.SelectTerminal;
import org.litebridgedb.orm.api.select.model.SelectSpec;

public class LimitClauseTerminalImpl<DTO, SSP extends SelectSpec>
        extends DelegatingSelector<DTO, SSP>
        implements LimitClauseTerminal<DTO> {

    protected final SSP selectSpec;

    public LimitClauseTerminalImpl(final AbstractSelector<DTO, SSP> delegate) {
        super(delegate);
        selectSpec = delegate.selectSpec();
    }

    @Override
    public SelectTerminal<DTO> offset(final int offset) {
        selectSpec().ensureLimit().setOffset(offset);
        return this;
    }

    protected SelectSpec selectSpec() {
        return selectSpec;
    }
}
