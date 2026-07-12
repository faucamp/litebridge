package org.litebridge.orm.api.dto;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.api.dto.condition.DtoConditionClauseStart;
import org.litebridge.orm.api.select.HavingConditionClauseTerminal;
import org.litebridge.orm.api.select.impl.AbstractHavingClauseTerminal;
import org.litebridge.orm.api.select.model.ConditionGroupSpec;
import org.litebridge.orm.api.select.model.ConditionSpec;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;
import org.litebridge.orm.persistence.OrmTable;

/**
 * Terminal clause for DTO HAVING conditions.
 *
 * @param <DTO> the type of the DTO
 */
public final class DtoHavingConditionClauseTerminal<DTO>
        extends AbstractHavingClauseTerminal<DTO,
        DtoOrderByClause<DTO>,
        DtoOrderByClauseChain<DTO>,
        DtoSelectSpec>

        implements HavingConditionClauseTerminal<DTO,
        DtoHavingConditionClause<DTO>,
        DtoHavingConditionClauseTerminal<DTO>,
        DtoOrderByClause<DTO>,
        DtoOrderByClauseChain<DTO>> {

    private final OrmTable ormTable;

    /**
     * Creates a new DtoHavingConditionClauseTerminal.
     *
     * @param delegate the DTO selector delegate
     */
    public DtoHavingConditionClauseTerminal(final DtoSelector<DTO> delegate) {
        super(delegate);
        ormTable = delegate.table();
    }

    /**
     * Adds an "AND" condition to the current condition clause using the specified column.
     * This method is used to chain additional conditions in a SQL query in a type-safe and fluent manner.
     *
     * @param field the name of the DTO field to be used in the "AND" condition
     * @return the parent condition clause interface, allowing further chaining of conditions
     */
    @Override
    public DtoHavingConditionClause<DTO> and(final String field) {
        final Column column = ormTable.getColumnForFieldName(field).toColumn();
        return and(new SelectColumnSpec(column));
    }

    @Override
    public DtoHavingConditionClause<DTO> and(final ExpressionSpec expression) {
        return havingImpl(LogicOperator.AND, expression);
    }

    @Override
    public DtoHavingConditionClauseTerminal<DTO> and(final QueryConditionBuilder<DTO> query) {
        return havingImpl(LogicOperator.AND, query);
    }

    @Override
    public DtoHavingConditionClause<DTO> or(final String field) {
        final Column column = ormTable.getColumnForFieldName(field).toColumn();
        return or(new SelectColumnSpec(column));
    }

    @Override
    public DtoHavingConditionClause<DTO> or(final ExpressionSpec expression) {
        return havingImpl(LogicOperator.OR, expression);
    }

    @Override
    public DtoHavingConditionClauseTerminal<DTO> or(final QueryConditionBuilder<DTO> query) {
        return havingImpl(LogicOperator.OR, query);
    }

    @Override
    public DtoOrderByClause<DTO> orderBy(final String... fields) {
        return new DtoOrderByClause<>(selectSpec.newOrderBy(selectSpec.createSelectFieldSpecs(fields)), (DtoSelector<DTO>) delegate);
    }

    @Override
    public DtoOrderByClause<DTO> orderBy(final ExpressionSpec... fields) {
        return new DtoOrderByClause<>(selectSpec.newOrderBy(fields), (DtoSelector<DTO>) delegate);
    }

    private DtoHavingConditionClause<DTO> havingImpl(final LogicOperator logicOperator, final ExpressionSpec expression) {
        final ConditionSpec conditionSpec = selectSpec.currentHavingConditionGroupSpec().newCondition(logicOperator, expression);
        return new DtoHavingConditionClause<>(conditionSpec, this, delegate.litebridgeContext());
    }

    private DtoHavingConditionClauseTerminal<DTO> havingImpl(final LogicOperator logicOperator, final QueryConditionBuilder<DTO> query) {
        final ConditionGroupSpec subgroup = selectSpec.pushHavingConditionGroup(logicOperator);
        final DtoConditionClauseStart<DTO> conditionClauseStart = new DtoConditionClauseStart<>(subgroup, ormTable, delegate.litebridgeContext().fromClauseEngine());
        query.apply(conditionClauseStart);
        selectSpec.popHavingConditionGroup();
        return this;
    }
}
