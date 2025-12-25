package org.litebridge.orm.api.select.impl;

import org.litebridge.orm.api.select.WhereConditionClause;
import org.litebridge.orm.api.select.WhereConditionClauseTerminal;

public class WhereConditionClauseTerminalImpl<DTO>
        extends WhereClauseTerminalImpl<DTO>
        implements WhereConditionClauseTerminal<DTO> {

    public WhereConditionClauseTerminalImpl(final AbstractSelector<DTO> delegate) {
        super(delegate);
    }
    
    @Override
    public WhereConditionClause<DTO> and(final String column) {
        return new WhereConditionClauseImpl<>(selectSpec.newWhereCondition(column), this);
    }
}
