package org.litebridge.orm.api.select.impl;

import org.litebridge.orm.api.select.JoinClause;
import org.litebridge.orm.api.select.JoinClauseTerminal;
import org.litebridge.orm.api.select.WhereConditionClause;

public class JoinClauseTerminalImpl<DTO>
        extends WhereConditionClauseTerminalImpl<DTO>
        implements JoinClauseTerminal<DTO> {

    public JoinClauseTerminalImpl(final AbstractSelector<DTO> delegate) {
        super(delegate);
    }

    @Override
    public WhereConditionClause<DTO> where(final String column) {
        return new WhereConditionClauseImpl<>(selectSpec.newWhereCondition(column), new WhereConditionClauseTerminalImpl<>(this));
    }

    @Override
    public JoinClause<DTO> join(final String table) {
        return new JoinClauseImpl<>(selectSpec.newJoinSpec(table), this);
    }
}
