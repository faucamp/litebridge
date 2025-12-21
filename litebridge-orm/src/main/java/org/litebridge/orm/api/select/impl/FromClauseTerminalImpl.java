package org.litebridge.orm.api.select.impl;

import org.litebridge.orm.api.select.FromClauseTerminal;
import org.litebridge.orm.api.select.JoinClause;
import org.litebridge.orm.api.select.SelectTerminal;
import org.litebridge.orm.api.select.WhereConditionClause;

public class FromClauseTerminalImpl<DTO> extends DelegatingSelectTerminal<DTO> implements FromClauseTerminal<DTO> {

    private final SelectSpec selectSpec;

    public FromClauseTerminalImpl(final SelectSpec selectSpec, final SelectTerminal<DTO> selectTerminal) {
        super(selectTerminal);
        this.selectSpec = selectSpec;
    }

    @Override
    public JoinClause<DTO> join(final String table) {
        return null;
    }

    @Override
    public WhereConditionClause<DTO> where(final String column) {
        return null;
    }
}
