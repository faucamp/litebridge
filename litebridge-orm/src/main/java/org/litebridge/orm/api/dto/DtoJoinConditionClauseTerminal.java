package org.litebridge.orm.api.dto;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.AbstractCbConditionClauseTerminal;
import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.api.dto.condition.DtoConditionClauseStart;
import org.litebridge.orm.api.select.JoinClauseTerminal;
import org.litebridge.orm.api.select.ast.ConditionGroupNode;
import org.litebridge.orm.api.select.ast.GroupByNode;
import org.litebridge.orm.api.select.ast.JoinNode;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.ast.WhereNode;
import org.litebridge.orm.api.select.impl.AbstractJoinConditionClauseTerminal;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.TableRegistry;

import java.util.function.Function;

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
    private final TableRegistry tableRegistry;

    /**
     * Creates a new instance of {@code DtoJoinConditionClauseTerminal}.
     *
     * @param joinNode
     * @param delegate the selector delegate
     */
    public DtoJoinConditionClauseTerminal(final JoinNode joinNode, final DtoSelector<DTO> delegate) {
        super(joinNode, delegate);
        this.ormTable = delegate.ormTable();
        this.tableRegistry = delegate.tableRegistry();
    }

    @Override
    public DtoJoinConditionClause<DTO> and(final String field) {
        final Column column = ormTable.getColumnForFieldName(field).toColumn();
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
        final Column column = ormTable.getColumnForFieldName(field).toColumn();
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
        final Column column = ormTable.getColumnForFieldName(field).toColumn();
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
        final OrmTable contextScopedTable = ormTable.getContextTableRegistry().getOrmTable(dtoClass);

        if (contextScopedTable != null) {
            joinTable = contextScopedTable;
        } else {
            joinTable = tableRegistry.getTableOrThrow(dtoClass);
        }

        return new DtoJoinClause<>((DtoSelector<DTO>) delegate, joinTable, node -> {
            final JoinNode joinNode = new JoinNode(delegate.node(), "INNER", joinTable.dtoClass(), ormTable.dtoClass(), null);
            joinNode.withCondition(node);
            delegate.withNode(joinNode);
            return new DtoJoinConditionClauseTerminal<>(joinNode, (DtoSelector<DTO>) delegate);
        });
    }

    @Override
    public DtoGroupByClauseTerminal<DTO> groupBy(final String... fields) {
        return groupBy(((DtoSelector<DTO>) delegate).createSelectFieldSpecs(fields).toArray(ExpressionSpec[]::new));
    }

    @Override
    public DtoGroupByClauseTerminal<DTO> groupBy(final ExpressionSpec... fields) {
        final QueryNode groupByNode = new GroupByNode(delegate.node(), fields);
        return new DtoGroupByClauseTerminal<>((DtoSelector<DTO>) delegate.withNode(groupByNode));
    }

    @Override
    public DtoOrderByClause<DTO> orderBy(final String... fields) {
        return orderBy(((DtoSelector<DTO>) delegate).createSelectFieldSpecs(fields).toArray(ExpressionSpec[]::new));
    }

    @Override
    public DtoOrderByClause<DTO> orderBy(final ExpressionSpec... fields) {
        return new DtoOrderByClause<>(fields, (DtoSelector<DTO>) delegate);
    }

    private DtoJoinConditionClause<DTO> joinImpl(final LogicOperator logicOperator, final ExpressionSpec expression) {
        final Function<QueryNode, DtoJoinConditionClauseTerminal<DTO>> recreator = n -> {
            joinNode.withCondition(n);
            return this;
        };
        return new DtoJoinConditionClause<>(delegate.litebridgeContext(), logicOperator, expression, joinNode.condition(), recreator);
    }

    private DtoWhereConditionClause<DTO> whereImpl(final LogicOperator logicOperator, final ExpressionSpec expression, final DtoSelector<DTO> newDelegate) {
        return new DtoWhereConditionClause<>(delegate.litebridgeContext(),
                logicOperator,
                expression,
                null,
                node -> new DtoWhereConditionClauseTerminal<>((DtoSelector<DTO>) delegate.withNode(new WhereNode(delegate.node(), node))));
    }

    private DtoJoinConditionClauseTerminal<DTO> joinImpl(final LogicOperator logicOperator, final QueryConditionBuilder<DTO> query) {
        final DtoConditionClauseStart<DTO> conditionClauseStart = new DtoConditionClauseStart<>(ormTable, delegate.litebridgeContext().fromClauseEngine(), null);
        final AbstractCbConditionClauseTerminal<DTO> terminal = query.apply(conditionClauseStart);
        final QueryNode conditionNode = terminal.node();

        final ConditionGroupNode groupNode = new ConditionGroupNode(joinNode.condition(), logicOperator, conditionNode);
        joinNode.withCondition(groupNode);

        return this;
    }
}
