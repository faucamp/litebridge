package org.litebridge.orm.api.select.impl;

import org.litebridge.orm.api.select.FromClauseTerminal;
import org.litebridge.orm.api.select.JoinClause;
import org.litebridge.orm.api.select.SelectTerminal;
import org.litebridge.orm.api.select.WhereConditionClause;

public class FromClauseTerminalImpl<DTO>
        extends JoinClauseTerminalImpl<DTO>
        implements FromClauseTerminal<DTO> {

    public FromClauseTerminalImpl(final AbstractSelector<DTO> delegate) {
        super(delegate);
    }
}
