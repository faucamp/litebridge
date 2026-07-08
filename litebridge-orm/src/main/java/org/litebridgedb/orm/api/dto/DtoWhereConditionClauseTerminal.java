package org.litebridgedb.orm.api.dto;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.query.LogicOperator;
import org.litebridgedb.orm.api.select.WhereConditionClauseTerminal;
import org.litebridgedb.orm.api.select.impl.AbstractWhereClauseTerminal;
import org.litebridgedb.orm.api.select.model.ConditionGroupSpec;
import org.litebridgedb.orm.api.select.model.GroupBySpec;
import org.litebridgedb.orm.expression.ExpressionSpec;
import org.litebridgedb.orm.expression.select.SelectColumnSpec;
import org.litebridgedb.orm.persistence.OrmTable;

public final class DtoWhereConditionClauseTerminal<DTO>
        extends AbstractWhereClauseTerminal<DTO,
        DtoGroupByClauseTerminal<DTO>,
        DtoHavingConditionClause<DTO>,
        DtoHavingConditionClauseTerminal<DTO>,
        DtoOrderByClause<DTO>,
        DtoOrderByClauseChain<DTO>,
        DtoSelectSpec>

        implements WhereConditionClauseTerminal<DTO,
        DtoWhereConditionClause<DTO>,
        DtoWhereConditionClauseTerminal<DTO>,
        DtoGroupByClauseTerminal<DTO>,
        DtoHavingConditionClause<DTO>,
        DtoHavingConditionClauseTerminal<DTO>,
        DtoOrderByClause<DTO>,
        DtoOrderByClauseChain<DTO>> {

    private final OrmTable table;

    public DtoWhereConditionClauseTerminal(final DtoSelector<DTO> delegate) {
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
    public DtoWhereConditionClause<DTO> and(final String field) {
        final Column column = table.getColumnForFieldName(field).toColumn();
        return and(new SelectColumnSpec(column));
    }

    /**
     * Adds an "AND" condition to the current condition clause using the specified column.
     * This method is used to chain additional conditions in a SQL query in a type-safe and fluent manner.
     *
     * @param expression the exoression to be used in the "AND" condition
     * @return the parent condition clause interface, allowing further chaining of conditions
     */
    @Override
    public DtoWhereConditionClause<DTO> and(final ExpressionSpec expression) {
        return whereImpl(LogicOperator.AND, expression);
    }

    @Override
    public DtoWhereConditionClause<DTO> or(final String field) {
        final Column spiColumn = table.getColumnForFieldName(field).toColumn();
        return or(new SelectColumnSpec(spiColumn));
    }

    @Override
    public DtoWhereConditionClause<DTO> or(final ExpressionSpec expression) {
        return whereImpl(LogicOperator.OR, expression);
    }

    @Override
    public DtoGroupByClauseTerminal<DTO> groupBy(final String... columns) {
        selectSpec.setGroupBy(new GroupBySpec(selectSpec.createSelectFieldSpecs(columns)));
        return new DtoGroupByClauseTerminal<>((DtoSelector<DTO>) delegate);
    }

    @Override
    public DtoGroupByClauseTerminal<DTO> groupBy(final ExpressionSpec... fields) {
        selectSpec.setGroupBy(new GroupBySpec(fields));
        return new DtoGroupByClauseTerminal<>((DtoSelector<DTO>) delegate);
    }

    @Override
    public DtoOrderByClause<DTO> orderBy(final String... fields) {
        return new DtoOrderByClause<>(selectSpec.newOrderBy(selectSpec.createSelectFieldSpecs(fields)), (DtoSelector<DTO>) delegate);
    }

    @Override
    public DtoOrderByClause<DTO> orderBy(final ExpressionSpec... fields) {
        return new DtoOrderByClause<>(selectSpec.newOrderBy(fields), (DtoSelector<DTO>) delegate);
    }

    private DtoWhereConditionClause<DTO> whereImpl(final LogicOperator logicOperator, final ExpressionSpec expression) {
        final ConditionGroupSpec conditionGroupSpec = selectSpec.newWhereConditionGroup(logicOperator);
        return new DtoWhereConditionClause<>(conditionGroupSpec.newCondition(expression), new DtoWhereConditionClauseTerminal<>((DtoSelector<DTO>) delegate), delegate.litebridgeContext());
    }
}
