package org.litebridge.orm.api.dto;

import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.select.impl.AbstractGroupByClauseTerminal;
import org.litebridge.orm.api.select.model.ConditionSpec;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.persistence.OrmTable;

/**
 * Terminal clause for DTO GROUP BY clauses.
 *
 * @param <DTO> the type of the DTO
 */
public class DtoGroupByClauseTerminal<DTO> extends AbstractGroupByClauseTerminal<DTO,
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
        this.ormTable = delegate.table();
    }

    @Override
    public DtoHavingConditionClause<DTO> having(final ExpressionSpec expression) {
        final ConditionSpec conditionSpec = selectSpec.currentHavingConditionGroupSpec().newCondition(LogicOperator.NOOP, expression);
        return new DtoHavingConditionClause<>(conditionSpec,
                new DtoHavingConditionClauseTerminal<>((DtoSelector<DTO>) delegate),
                delegate.litebridgeContext());
    }

    @Override
    public DtoOrderByClause<DTO> orderBy(final String... fields) {
        return new DtoOrderByClause<>(selectSpec.newOrderBy(selectSpec.createSelectFieldSpecs(fields)), (DtoSelector<DTO>) delegate);
    }

    @Override
    public DtoOrderByClause<DTO> orderBy(final ExpressionSpec... fields) {
        return new DtoOrderByClause<>(selectSpec.newOrderBy(fields), (DtoSelector<DTO>) delegate);
    }
}
