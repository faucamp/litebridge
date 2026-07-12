package org.litebridge.orm.api.dto;

import org.litebridge.orm.api.select.OrderByClause;
import org.litebridge.orm.api.select.model.OrderBySpec;

/**
 * Represents an ORDER BY clause in a DTO-based query.
 *
 * @param <DTO> the type of the DTO being queried
 */
public final class DtoOrderByClause<DTO>
        implements OrderByClause<DTO, DtoOrderByClause<DTO>, DtoOrderByClauseChain<DTO>> {

    private final OrderBySpec orderBySpec;
    private final DtoSelector<DTO> delegate;

    /**
     * Creates a new instance of {@code DtoOrderByClause}.
     *
     * @param orderBySpec the order by specification
     * @param delegate the selector delegate
     */
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
