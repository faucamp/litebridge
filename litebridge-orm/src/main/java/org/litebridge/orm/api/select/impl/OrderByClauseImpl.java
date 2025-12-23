package org.litebridge.orm.api.select.impl;

import org.litebridge.orm.api.select.model.OrderBySpec;
import org.litebridge.orm.api.select.OrderByClause;
import org.litebridge.orm.api.select.OrderByClauseChain;

public class OrderByClauseImpl<DTO> implements OrderByClause<DTO> {

    private final OrderBySpec orderBySpec;
    private final AbstractSelector<DTO> delegate;

    public OrderByClauseImpl(final OrderBySpec orderBySpec, final AbstractSelector<DTO> delegate) {
        this.orderBySpec = orderBySpec;
        this.delegate = delegate;
    }

    @Override
    public OrderByClauseChain<DTO> asc() {
        orderBySpec.setAsc(true);
        return new OrderByClauseChainImpl<>(delegate);
    }

    @Override
    public OrderByClauseChain<DTO> desc() {
        orderBySpec.setAsc(false);
        return new OrderByClauseChainImpl<>(delegate);
    }
}
