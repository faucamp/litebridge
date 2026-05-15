package org.litebridgedb.orm.api.dto;

import org.litebridgedb.orm.api.select.OrderByClause;
import org.litebridgedb.orm.api.select.impl.AbstractSelector;
import org.litebridgedb.orm.api.select.model.OrderBySpec;

public final class DtoOrderByClause<DTO>
        implements OrderByClause<DTO, DtoOrderByClause<DTO>, DtoOrderByClauseChain<DTO>> {

    private final OrderBySpec orderBySpec;
    private final AbstractSelector<DTO, DtoSelectSpec> delegate;

    public DtoOrderByClause(final OrderBySpec orderBySpec, final AbstractSelector<DTO, DtoSelectSpec> delegate) {
        this.orderBySpec = orderBySpec;
        this.delegate = delegate;
    }

    @Override
    public DtoOrderByClauseChain<DTO> asc() {
        orderBySpec.setAsc(true);
        return new DtoOrderByClauseChain<>((DtoSelector<DTO>) delegate);
    }

    @Override
    public DtoOrderByClauseChain<DTO> desc() {
        orderBySpec.setAsc(false);
        return new DtoOrderByClauseChain<>((DtoSelector<DTO>) delegate);
    }
}
