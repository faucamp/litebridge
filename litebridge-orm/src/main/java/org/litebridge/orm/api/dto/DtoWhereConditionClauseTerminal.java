package org.litebridge.orm.api.dto;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.api.dto.condition.DtoConditionClauseStart;
import org.litebridge.orm.api.select.WhereConditionClauseTerminal;
import org.litebridge.orm.api.select.impl.AbstractWhereClauseTerminal;
import org.litebridge.orm.api.select.model.ConditionGroupSpec;
import org.litebridge.orm.api.select.model.ConditionSpec;
import org.litebridge.orm.api.select.model.GroupBySpec;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;
import org.litebridge.orm.persistence.OrmTable;

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

    private final OrmTable ormTable;

    public DtoWhereConditionClauseTerminal(final DtoSelector<DTO> delegate) {
        super(delegate);
        ormTable = delegate.table();
    }

    @Override
    public DtoWhereConditionClause<DTO> and(final String field) {
        final Column column = ormTable.getColumnForFieldName(field).toColumn();
        return and(new SelectColumnSpec(column));
    }

    @Override
    public DtoWhereConditionClause<DTO> and(final ExpressionSpec expression) {
        return whereImpl(LogicOperator.AND, expression);
    }

    @Override
    public DtoWhereConditionClauseTerminal<DTO> and(final QueryConditionBuilder<DTO> query) {
        return whereImpl(LogicOperator.AND, query);
    }

    @Override
    public DtoWhereConditionClause<DTO> or(final String field) {
        final Column spiColumn = ormTable.getColumnForFieldName(field).toColumn();
        return or(new SelectColumnSpec(spiColumn));
    }

    @Override
    public DtoWhereConditionClause<DTO> or(final ExpressionSpec expression) {
        return whereImpl(LogicOperator.OR, expression);
    }

    @Override
    public DtoWhereConditionClauseTerminal<DTO> or(final QueryConditionBuilder<DTO> query) {
        return whereImpl(LogicOperator.OR, query);
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
        final ConditionSpec conditionSpec = selectSpec.currentWhereConditionGroupSpec().newCondition(logicOperator, expression);
        return new DtoWhereConditionClause<>(conditionSpec, new DtoWhereConditionClauseTerminal<>((DtoSelector<DTO>) delegate), delegate.litebridgeContext());
    }

    private DtoWhereConditionClauseTerminal<DTO> whereImpl(final LogicOperator logicOperator, final QueryConditionBuilder<DTO> query) {
        final ConditionGroupSpec conditionGroupSpec = selectSpec.pushWhereConditionGroup(logicOperator);
        final DtoConditionClauseStart<DTO> conditionClauseStart = new DtoConditionClauseStart<>(conditionGroupSpec, ormTable, delegate.litebridgeContext().fromClauseEngine());
        query.apply(conditionClauseStart);
        selectSpec.popWhereConditionGroup();
        return this;
    }
}
