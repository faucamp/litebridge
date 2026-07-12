package org.litebridge.orm.api.dto;

import org.litebridge.orm.api.select.OrderByClause;
import org.litebridge.orm.api.select.model.OrderBySpec;

public final class DtoOrderByClause<DTO>
        implements OrderByClause<DTO, DtoOrderByClause<DTO>, DtoOrderByClauseChain<DTO>> {

    private final OrderBySpec orderBySpec;
    private final DtoSelector<DTO> delegate;

    public DtoOrderByClause(final OrderBySpec orderBySpec, final DtoSelector<DTO> delegate) {
        this.orderBySpec = orderBySpec;
        this.delegate = delegate;
    }

    @Override
    public DtoOrderByClauseChain<DTO> asc() {
        orderBySpec.setAsc(true);
        return new DtoOrderByClauseChain<>(delegate);
    }

    @Override
    public DtoOrderByClauseChain<DTO> desc() {
        orderBySpec.setAsc(false);
        return new DtoOrderByClauseChain<>(delegate);
    }
}
