package org.litebridge.orm.api.select.impl;

import org.litebridge.orm.api.select.OrderByClause;
import org.litebridge.orm.api.select.OrderByClauseChain;

public class OrderByClauseChainImpl<DTO>
        extends OrderByClauseTerminalImpl<DTO>
        implements OrderByClauseChain<DTO> {

    public OrderByClauseChainImpl(final AbstractSelector<DTO> delegate) {
        super(delegate);
    }

    @Override
    public OrderByClause<DTO> then(final String... columns) {
        return new OrderByClauseImpl<>(selectSpec.newOrderBy(columns), delegate);
    }
}
