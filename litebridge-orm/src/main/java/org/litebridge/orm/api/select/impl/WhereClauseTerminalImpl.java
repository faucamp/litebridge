package org.litebridge.orm.api.select.impl;

import org.litebridge.orm.api.select.OrderByClause;
import org.litebridge.orm.api.select.WhereClauseTerminal;

public class WhereClauseTerminalImpl<DTO>
        extends OrderByClauseTerminalImpl<DTO>
        implements WhereClauseTerminal<DTO> {

    public WhereClauseTerminalImpl(final AbstractSelector<DTO> delegate) {
        super(delegate);
    }

    @Override
    public OrderByClause<DTO> orderBy(final String... column) {
        return new OrderByClauseImpl<>(selectSpec.newOrderBy(column), this);
    }
}
