package org.litebridgedb.orm.api.dto;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.query.LogicOperator;
import org.litebridgedb.orm.api.select.HavingConditionClauseTerminal;
import org.litebridgedb.orm.api.select.impl.AbstractHavingClauseTerminal;
import org.litebridgedb.orm.api.select.model.ConditionGroupSpec;
import org.litebridgedb.orm.expression.ExpressionSpec;
import org.litebridgedb.orm.expression.select.SelectColumnSpec;
import org.litebridgedb.orm.persistence.OrmTable;

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

    private final OrmTable table;

    public DtoHavingConditionClauseTerminal(final DtoSelector<DTO> delegate) {
        super(delegate);
        table = delegate.table();
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
        final Column column = table.getColumnForFieldName(field).toColumn();
        return and(new SelectColumnSpec(column));
    }

    @Override
    public DtoHavingConditionClause<DTO> and(final ExpressionSpec expression) {
        return havingImpl(LogicOperator.AND, expression);
    }

    @Override
    public DtoHavingConditionClause<DTO> or(final String field) {
        final Column column = table.getColumnForFieldName(field).toColumn();
        return or(new SelectColumnSpec(column));
    }

    @Override
    public DtoHavingConditionClause<DTO> or(final ExpressionSpec expression) {
        return havingImpl(LogicOperator.OR, expression);
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
        final ConditionGroupSpec conditionGroupSpec = selectSpec.newHavingConditionGroup(logicOperator);
        return new DtoHavingConditionClause<>(conditionGroupSpec.newCondition(expression), this, delegate.litebridgeContext());
    }
}
