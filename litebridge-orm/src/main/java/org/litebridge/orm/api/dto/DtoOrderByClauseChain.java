package org.litebridge.orm.api.dto;

import org.litebridge.orm.api.select.OrderByClauseChain;
import org.litebridge.orm.api.select.impl.OrderByClauseTerminalImpl;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.persistence.OrmTable;

/**
 * Represents a chain of ORDER BY clauses in a DTO-based query.
 *
 * @param <DTO> the type of the DTO being queried
 */
public final class DtoOrderByClauseChain<DTO>
        extends OrderByClauseTerminalImpl<DTO, DtoSelectSpec>
        implements OrderByClauseChain<DTO, DtoOrderByClause<DTO>, DtoOrderByClauseChain<DTO>> {

    private final OrmTable table;

    /**
     * Creates a new instance of {@code DtoOrderByClauseChain}.
     *
     * @param delegate the selector delegate
     */
    public DtoOrderByClauseChain(final DtoSelector<DTO> delegate) {
        super(delegate);
        table = delegate.ormTable();
    }

    @Override
    public DtoOrderByClause<DTO> then(final String... fields) {
        return new DtoOrderByClause<>(((DtoSelector<DTO>) delegate).createSelectFieldSpecs(fields).toArray(ExpressionSpec[]::new), (DtoSelector<DTO>) delegate);
    }

    @Override
    public DtoOrderByClause<DTO> then(final ExpressionSpec... fields) {
        return new DtoOrderByClause<>(fields, (DtoSelector<DTO>) delegate);
    }
}
