package org.litebridgedb.orm.api.dto;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.query.LogicOperator;
import org.litebridgedb.orm.api.condition.QueryConditionBuilder;
import org.litebridgedb.orm.api.dto.condition.DtoConditionClauseStart;
import org.litebridgedb.orm.api.select.JoinClauseTerminal;
import org.litebridgedb.orm.api.select.impl.AbstractJoinConditionClauseTerminal;
import org.litebridgedb.orm.api.select.model.ConditionGroupSpec;
import org.litebridgedb.orm.api.select.model.ConditionSpec;
import org.litebridgedb.orm.api.select.model.GroupBySpec;
import org.litebridgedb.orm.expression.ExpressionSpec;
import org.litebridgedb.orm.expression.select.SelectColumnSpec;
import org.litebridgedb.orm.persistence.OrmTable;
import org.litebridgedb.orm.persistence.TableRegistry;
import org.litebridgedb.orm.persistence.alias.AliasGenerator;

public final class DtoJoinConditionClauseTerminal<DTO>
        extends AbstractJoinConditionClauseTerminal<DTO,
        DtoJoinConditionClause<DTO>,
        DtoJoinConditionClauseTerminal<DTO>,
        DtoGroupByClauseTerminal<DTO>,
        DtoHavingConditionClause<DTO>,
        DtoHavingConditionClauseTerminal<DTO>,
        DtoOrderByClause<DTO>,
        DtoOrderByClauseChain<DTO>,
        DtoSelectSpec,
        DtoJoinSpec>

        implements JoinClauseTerminal<DTO,
        DtoJoinClause<DTO>,
        DtoJoinConditionClause<DTO>,
        DtoJoinConditionClauseTerminal<DTO>,
        DtoWhereConditionClause<DTO>,
        DtoWhereConditionClauseTerminal<DTO>,
        DtoGroupByClauseTerminal<DTO>,
        DtoHavingConditionClause<DTO>,
        DtoHavingConditionClauseTerminal<DTO>,
        DtoOrderByClause<DTO>,
        DtoOrderByClauseChain<DTO>>,

        DtoJoinClassTerminal<DTO> {

    private final OrmTable ormTable;
    private final AliasGenerator aliasGenerator;
    private final TableRegistry tableRegistry;

    public DtoJoinConditionClauseTerminal(final DtoJoinSpec joinSpec, final DtoSelector<DTO> delegate, final AliasGenerator aliasGenerator) {
        super(joinSpec, delegate);
        this.ormTable = delegate.table();
        this.aliasGenerator = aliasGenerator;
        this.tableRegistry = delegate.tableRegistry();
    }

    @Override
    public DtoJoinConditionClause<DTO> and(final String field) {
        final Column column = aliasGenerator.aliasColumn(selectSpec.getTable(), ormTable.getColumnForFieldName(field));
        return and(new SelectColumnSpec(column));
    }

    @Override
    public DtoJoinConditionClause<DTO> and(final ExpressionSpec expression) {
        return joinImpl(LogicOperator.AND, expression);
    }

    @Override
    public DtoJoinConditionClauseTerminal<DTO> and(final QueryConditionBuilder<DTO> query) {
        return joinImpl(LogicOperator.AND, query);
    }

    @Override
    public DtoJoinConditionClause<DTO> or(final String field) {
        final Column column = aliasGenerator.aliasColumn(selectSpec.getTable(), ormTable.getColumnForFieldName(field));
        return or(new SelectColumnSpec(column));
    }

    @Override
    public DtoJoinConditionClause<DTO> or(final ExpressionSpec expression) {
        return joinImpl(LogicOperator.OR, expression);
    }

    @Override
    public DtoJoinConditionClauseTerminal<DTO> or(final QueryConditionBuilder<DTO> query) {
        return joinImpl(LogicOperator.OR, query);
    }

    @Override
    public DtoWhereConditionClause<DTO> where(final String field) {
        final Column column = aliasGenerator.aliasColumn(selectSpec.getTable(), ormTable.getColumnForFieldName(field));
        return where(new SelectColumnSpec(column));
    }

    @Override
    public DtoWhereConditionClause<DTO> where(final ExpressionSpec expression) {
        return whereImpl(LogicOperator.NOOP, expression);
    }


    @Override
    public DtoJoinClause<DTO> join(final Class<?> dtoClass) {
        final OrmTable joinTable;

        // First check for inline/contextually-registered tables
        final OrmTable contextScopedTable = ormTable.getContextTableRegistry().getTable(dtoClass);

        if (contextScopedTable != null) {
            joinTable = contextScopedTable;
        } else {
            joinTable = tableRegistry.getTableOrThrow(dtoClass);
        }

        return new DtoJoinClause<>(dtoClass, joinTable, (DtoSelector<DTO>) delegate);
    }

    @Override
    public DtoGroupByClauseTerminal<DTO> groupBy(final String... fields) {
        selectSpec.setGroupBy(new GroupBySpec(selectSpec.createSelectFieldSpecs(fields)));
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

    private DtoJoinConditionClause<DTO> joinImpl(final LogicOperator logicOperator, final ExpressionSpec expression) {
        final ConditionSpec conditionSpec = joinSpec.currentConditionGroupSpec().newCondition(logicOperator, expression);
        return new DtoJoinConditionClause<>(conditionSpec, this, delegate.litebridgeContext());
    }

    private DtoWhereConditionClause<DTO> whereImpl(final LogicOperator logicOperator, final ExpressionSpec expression) {
        final ConditionSpec conditionSpec = selectSpec.currentWhereConditionGroupSpec().newCondition(logicOperator, expression);
        return new DtoWhereConditionClause<>(conditionSpec, new DtoWhereConditionClauseTerminal<>((DtoSelector<DTO>) delegate), delegate.litebridgeContext());
    }

    private DtoJoinConditionClauseTerminal<DTO> joinImpl(final LogicOperator logicOperator, final QueryConditionBuilder<DTO> query) {
        final ConditionGroupSpec subgroup = joinSpec.pushConditionGroupSpec(logicOperator);
        final DtoConditionClauseStart<DTO> conditionClauseStart = new DtoConditionClauseStart<>(subgroup, joinSpec.dtoTable(), delegate.litebridgeContext().fromClauseEngine());
        query.apply(conditionClauseStart);
        return new DtoJoinConditionClauseTerminal<>(joinSpec, (DtoSelector<DTO>) delegate, aliasGenerator);
    }
}
