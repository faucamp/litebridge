package org.litebridge.orm.api.dto;

import org.litebridge.orm.api.select.OrderByClause;
import org.litebridge.orm.api.select.ast.OrderByNode;
import org.litebridge.orm.expression.ExpressionSpec;

/**
 * Represents an ORDER BY clause in a DTO-based query.
 *
 * @param <DTO> the type of the DTO being queried
 */
public final class DtoOrderByClause<DTO>
        implements OrderByClause<DTO, DtoOrderByClause<DTO>, DtoOrderByClauseChain<DTO>> {

    private final ExpressionSpec[] expressions;
    private final DtoSelector<DTO> delegate;

    /**
     * Creates a new instance of {@code DtoOrderByClause}.
     *
     * @param expressions the expressions to order by
     * @param delegate    the selector delegate
     */
    public DtoOrderByClause(final ExpressionSpec[] expressions, final DtoSelector<DTO> delegate) {
        this.expressions = expressions;
        this.delegate = delegate;
    }

    @Override
    public DtoOrderByClauseChain<DTO> asc() {
        for (final ExpressionSpec expression : expressions) {
            delegate.withNode(new OrderByNode(delegate.node(), expression, true));
        }

        return new DtoOrderByClauseChain<>(delegate);
    }

    @Override
    public DtoOrderByClauseChain<DTO> desc() {
        for (final ExpressionSpec expression : expressions) {
            delegate.withNode(new OrderByNode(delegate.node(), expression, false));
        }

        return new DtoOrderByClauseChain<>(delegate);
    }
}
