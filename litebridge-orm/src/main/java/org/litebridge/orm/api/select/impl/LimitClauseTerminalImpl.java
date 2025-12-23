package org.litebridge.orm.api.select.impl;

import org.litebridge.orm.api.select.LimitClauseTerminal;
import org.litebridge.orm.api.select.SelectTerminal;

public class LimitClauseTerminalImpl<DTO>
        extends AbstractSelector<DTO>
        implements LimitClauseTerminal<DTO> {

    public LimitClauseTerminalImpl(final AbstractSelector<DTO> delegate) {
        super(delegate);
    }

    @Override
    public SelectTerminal<DTO> offset(final int offset) {
        selectSpec.ensureLimit().setOffset(offset);
        return this;
    }
}
