package org.litebridge.orm.api.select.impl;

import org.litebridge.orm.api.select.ConditionClause;
import org.litebridge.orm.api.select.ConditionClauseTerminal;
import org.litebridge.orm.api.select.OrderByClause;
import org.litebridge.orm.api.select.SelectTerminal;
import org.litebridge.orm.api.select.WhereConditionClauseTerminal;

public class WhereConditionClauseTerminalImpl<DTO>
        extends WhereClauseTerminalImpl<DTO>
        implements WhereConditionClauseTerminal<DTO> {

    public WhereConditionClauseTerminalImpl(final AbstractSelector<DTO> delegate) {
        super(delegate);
    }

    @Override
    public ConditionClause<DTO, WhereConditionClauseTerminal<DTO>> and(final String column) {
        return new WhereConditionClauseImpl<>(selectSpec.newWhereCondition(column), this);
    }
}
