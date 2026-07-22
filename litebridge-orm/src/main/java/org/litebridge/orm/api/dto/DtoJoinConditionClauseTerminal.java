package org.litebridge.orm.api.dto;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.api.dto.condition.DtoConditionClauseStart;
import org.litebridge.orm.api.select.JoinClauseTerminal;
import org.litebridge.orm.api.select.ast.GroupByNode;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.ast.WhereNode;
import org.litebridge.orm.api.select.impl.AbstractJoinConditionClauseTerminal;
import org.litebridge.orm.api.select.model.ConditionGroupSpec;
import org.litebridge.orm.api.select.model.ConditionSpec;
import org.litebridge.orm.api.select.model.GroupBySpec;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.TableRegistry;
import org.litebridge.orm.persistence.alias.AliasGenerator;

/**
 * Represents the terminal part of a JOIN condition clause in a DTO-based query.
 *
 * @param <DTO> the type of the DTO being queried
 */
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

    /**
     * Creates a new instance of {@code DtoJoinConditionClauseTerminal}.
     *
     * @param joinSpec the join specification
     * @param delegate the selector delegate
     * @param aliasGenerator the alias generator
     */
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
        return whereImpl(LogicOperator.NOOP, expression, (DtoSelector<DTO>) delegate);
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

        final QueryNode joinNode = new org.litebridge.orm.api.select.ast.JoinNode(delegate.node(), "INNER", dtoClass, ormTable.dtoClass(), null);
        final DtoSelector<DTO> newDelegate = (DtoSelector<DTO>) delegate.withNode(joinNode);

        return new DtoJoinClause<>(dtoClass, joinTable, newDelegate);
    }

    @Override
    public DtoGroupByClauseTerminal<DTO> groupBy(final String... fields) {
        return groupBy(selectSpec.createSelectFieldSpecs(fields).toArray(ExpressionSpec[]::new));
    }

    @Override
    public DtoGroupByClauseTerminal<DTO> groupBy(final ExpressionSpec... fields) {
        final QueryNode groupByNode = new GroupByNode(delegate.node(), fields);
        return new DtoGroupByClauseTerminal<>((DtoSelector<DTO>) delegate.withNode(groupByNode));
    }

    @Override
    public DtoOrderByClause<DTO> orderBy(final String... fields) {
        return orderBy(selectSpec.createSelectFieldSpecs(fields).toArray(ExpressionSpec[]::new));
    }

    @Override
    public DtoOrderByClause<DTO> orderBy(final ExpressionSpec... fields) {
        return new DtoOrderByClause<>(fields, (DtoSelector<DTO>) delegate);
    }

    private DtoJoinConditionClause<DTO> joinImpl(final LogicOperator logicOperator, final ExpressionSpec expression) {
        final ConditionSpec conditionSpec = joinSpec.currentConditionGroupSpec().newCondition(logicOperator, expression);

        final java.util.function.Function<org.litebridge.orm.api.select.ast.QueryNode, DtoJoinConditionClauseTerminal<DTO>> recreator = n -> new DtoJoinConditionClauseTerminal<>(joinSpec, (DtoSelector<DTO>) delegate.withNode(n), aliasGenerator);

        return new DtoJoinConditionClause<>(conditionSpec, delegate.litebridgeContext(), logicOperator, expression, delegate.node(), recreator);
    }

    private DtoWhereConditionClause<DTO> whereImpl(final LogicOperator logicOperator, final ExpressionSpec expression, final DtoSelector<DTO> newDelegate) {
        final ConditionSpec conditionSpec = selectSpec.currentWhereConditionGroupSpec().newCondition(logicOperator, expression);

        return new DtoWhereConditionClause<>(conditionSpec,
                delegate.litebridgeContext(),
                logicOperator,
                expression,
                delegate.node(),
                node -> new DtoWhereConditionClauseTerminal<>((DtoSelector<DTO>) delegate.withNode(node)));
    }

    private DtoJoinConditionClauseTerminal<DTO> joinImpl(final LogicOperator logicOperator, final QueryConditionBuilder<DTO> query) {
        final ConditionGroupSpec subgroup = joinSpec.pushConditionGroupSpec(logicOperator);
        final DtoConditionClauseStart<DTO> conditionClauseStart = new DtoConditionClauseStart<>(subgroup, joinSpec.dtoTable(), delegate.litebridgeContext().fromClauseEngine());
        query.apply(conditionClauseStart);
        joinSpec.popConditionGroupSpec();
        return this;
    }
}
