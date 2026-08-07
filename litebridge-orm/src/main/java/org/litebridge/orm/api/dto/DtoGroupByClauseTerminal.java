package org.litebridge.orm.api.dto;

import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.select.ast.HavingNode;
import org.litebridge.orm.api.select.impl.AbstractGroupByClauseTerminal;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.persistence.OrmTable;

/**
 * Terminal clause for DTO GROUP BY clauses.
 *
 * @param <DTO> the type of the DTO
 */
public final class DtoGroupByClauseTerminal<DTO> extends AbstractGroupByClauseTerminal<DTO,
        DtoHavingConditionClause<DTO>,
        DtoHavingConditionClauseTerminal<DTO>,
        DtoOrderByClause<DTO>,
        DtoOrderByClauseChain<DTO>,
        DtoSelectSpec> {

    private final OrmTable ormTable;

    /**
     * Creates a new DtoGroupByClauseTerminal.
     *
     * @param delegate the DTO selector delegate
     */
    public DtoGroupByClauseTerminal(final DtoSelector<DTO> delegate) {
        super(delegate);
        this.ormTable = delegate.ormTable();
    }

    @Override
    public DtoHavingConditionClause<DTO> having(final ExpressionSpec expression) {
        return new DtoHavingConditionClause<>(delegate.litebridgeContext(),
                LogicOperator.NOOP,
                expression,
                null,
                node -> new DtoHavingConditionClauseTerminal<>((DtoSelector<DTO>) delegate.withNode(new HavingNode(delegate.node(), node))));
    }

    @Override
    public DtoOrderByClause<DTO> orderBy(final String... fields) {
        return orderBy(((DtoSelector<DTO>) delegate).createSelectFieldSpecs(fields).toArray(ExpressionSpec[]::new));
    }

    @Override
    public DtoOrderByClause<DTO> orderBy(final ExpressionSpec... fields) {
        return new DtoOrderByClause<>(fields, (DtoSelector<DTO>) delegate);
    }
}
