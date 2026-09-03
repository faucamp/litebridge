package org.litebridge.orm.api.dto;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.api.select.JoinClauseTerminal;
import org.litebridge.orm.engine.ast.JoinNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.ast.WhereNode;
import org.litebridge.orm.api.select.impl.AbstractJoinConditionClauseTerminal;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngineTerminal;
import org.litebridge.orm.expression.ExpressionSpec;

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
        DtoOrderByClauseChain<DTO>>

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

    /**
     * Creates a new instance of {@code DtoJoinConditionClauseTerminal}.
     *
     * @param joinNode
     */
    public DtoJoinConditionClauseTerminal(final JoinNode joinNode,
                                          final SelectEngineTerminal selectEngineTerminal,
                                          final LitebridgeContext litebridgeContext) {
        super(joinNode, selectEngineTerminal, litebridgeContext);
    }

    @Override
    public DtoJoinConditionClause<DTO> and(final String field) {
//        final Column column = ormTable.getColumnForFieldName(field).toColumn();
//        return and(new SelectColumnSpec(column));
        throw new UnsupportedOperationException("Not implemented yet");
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
//        final Column column = ormTable.getColumnForFieldName(field).toColumn();
//        return or(new SelectColumnSpec(column));
        throw new UnsupportedOperationException("Not implemented yet");
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
        return whereImpl(LogicOperator.NOOP, field, null);
    }

    @Override
    public DtoWhereConditionClause<DTO> where(final ExpressionSpec expression) {
        return whereImpl(LogicOperator.NOOP, null, expression);
    }


    @Override
    public DtoJoinClause<DTO> join(final Class<?> dtoClass) {
//        final OrmTable joinOrmTable;
//
//        // First check for inline/contextually-registered tables
//        final OrmTable contextScopedTable = ormTable.getContextTableRegistry().getOrmTable(dtoClass);
//
//        if (contextScopedTable != null) {
//            joinOrmTable = contextScopedTable;
//        } else {
//            joinOrmTable = tableRegistry.getTableOrThrow(dtoClass);
//        }

//        return new DtoJoinClause<>((DtoSelector<DTO>) delegate, joinOrmTable, node -> {
//            final JoinNode joinNode = new JoinNode(delegate.node(), "INNER", joinOrmTable.dtoClass(), ormTable.dtoClass(), null);
//            joinNode.withCondition(node);
//            delegate.withNode(joinNode);
//            return new DtoJoinConditionClauseTerminal<>(joinNode, (DtoSelector<DTO>) delegate);
//        });
        return new DtoJoinClause<>(null, litebridgeContext, conditionNode -> {
            final JoinNode joinNode = new JoinNode(node, "INNER", dtoClass, null);
            joinNode.withCondition(conditionNode);
            return new DtoJoinConditionClauseTerminal(joinNode, selectEngineTerminal, litebridgeContext);
        });
    }

    @Override
    public DtoGroupByClauseTerminal<DTO> groupBy(final String... fields) {
        return new DtoGroupByClauseTerminal<>(fields, node, selectEngineTerminal, litebridgeContext);
    }

    @Override
    public DtoGroupByClauseTerminal<DTO> groupBy(final ExpressionSpec... expressions) {
        return new DtoGroupByClauseTerminal<>(expressions, node, selectEngineTerminal, litebridgeContext);
    }

    @Override
    public DtoOrderByClause<DTO> orderBy(final String... fields) {
        return new DtoOrderByClause<>(fields, node, selectEngineTerminal, litebridgeContext);
    }

    @Override
    public DtoOrderByClause<DTO> orderBy(final ExpressionSpec... fields) {
        return new DtoOrderByClause<>(fields, node, selectEngineTerminal, litebridgeContext);
    }

    private DtoJoinConditionClause<DTO> joinImpl(final LogicOperator logicOperator, final ExpressionSpec expression) {
        final Function<QueryNode, DtoJoinConditionClauseTerminal<DTO>> recreator = n -> {
            joinNode.withCondition(n);
            return this;
        };

//        return new DtoJoinConditionClause<>(delegate.litebridgeContext(), logicOperator, expression, Objects.requireNonNull(joinNode.condition()), recreator);
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private DtoJoinConditionClauseTerminal<DTO> joinImpl(final LogicOperator logicOperator, final QueryConditionBuilder<DTO> query) {
//        final DtoConditionClauseStart<DTO> conditionClauseStart = new DtoConditionClauseStart<>(ormTable, delegate.litebridgeContext().fromClauseEngine(), null);
//        final AbstractCbConditionClauseTerminal<DTO> terminal = query.apply(conditionClauseStart);
//        final QueryNode conditionNode = terminal.node();
//
//        final ConditionGroupNode groupNode = new ConditionGroupNode(joinNode.condition(), logicOperator, conditionNode);
//        joinNode.withCondition(groupNode);
//
//        return this;
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private DtoWhereConditionClause<DTO> whereImpl(final LogicOperator logicOperator, final @Nullable String field, final @Nullable ExpressionSpec expression) {
        return new DtoWhereConditionClause<>(litebridgeContext,
                logicOperator,
                field,
                expression,
                null,
                conditionNode -> new DtoWhereConditionClauseTerminal<>(new WhereNode(this.node, conditionNode), selectEngineTerminal, litebridgeContext));
    }
}
