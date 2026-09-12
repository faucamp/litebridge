package org.litebridge.orm.api.select.dto;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.AbstractCbConditionClauseTerminal;
import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.api.condition.DtoConditionClauseStart;
import org.litebridge.orm.api.select.JoinClauseTerminal;
import org.litebridge.orm.api.select.impl.AbstractJoinConditionClauseTerminal;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngineTerminal;
import org.litebridge.orm.engine.ast.ConditionGroupNode;
import org.litebridge.orm.engine.ast.JoinNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.ast.WhereNode;
import org.litebridge.orm.expression.ExpressionSpec;

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
     * @param joinNode             the join query node
     * @param selectEngineTerminal the terminal select engine
     * @param litebridgeContext    the Litebridge context
     */
    public DtoJoinConditionClauseTerminal(final JoinNode joinNode,
                                          final SelectEngineTerminal selectEngineTerminal,
                                          final LitebridgeContext litebridgeContext) {
        super(joinNode, selectEngineTerminal, litebridgeContext);
    }

    @Override
    public DtoJoinConditionClause<DTO> and(final String field) {
        return joinImpl(LogicOperator.AND, field, null);
    }

    @Override
    public DtoJoinConditionClause<DTO> and(final ExpressionSpec expression) {
        return joinImpl(LogicOperator.AND, null, expression);
    }

    @Override
    public DtoJoinConditionClauseTerminal<DTO> and(final QueryConditionBuilder<DTO> query) {
        return joinImpl(LogicOperator.AND, query);
    }

    @Override
    public DtoJoinConditionClause<DTO> or(final String field) {
        return joinImpl(LogicOperator.OR, field, null);
    }

    @Override
    public DtoJoinConditionClause<DTO> or(final ExpressionSpec expression) {
        return joinImpl(LogicOperator.OR, null, expression);
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
        return new DtoJoinClause<>(null, litebridgeContext, conditionNode -> {
            final JoinNode joinNode = new JoinNode(node, "INNER", dtoClass, null);
            joinNode.setCondition(conditionNode);
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

    private DtoJoinConditionClause<DTO> joinImpl(final LogicOperator logicOperator, final @Nullable String field, final @Nullable ExpressionSpec expression) {
        return new DtoJoinConditionClause<>(
                litebridgeContext,
                logicOperator,
                field,
                expression,
                node,
                conditionNode -> {
                    joinNode.setCondition(conditionNode);
                    return this;
                });
    }

    private DtoJoinConditionClauseTerminal<DTO> joinImpl(final LogicOperator logicOperator, final QueryConditionBuilder<DTO> query) {
        final DtoConditionClauseStart<DTO> conditionClauseStart = new DtoConditionClauseStart<>(null, litebridgeContext);
        final AbstractCbConditionClauseTerminal<DTO> terminal = query.apply(conditionClauseStart);
        final QueryNode conditionNode = terminal.node();

        final ConditionGroupNode groupNode = new ConditionGroupNode(joinNode.condition(), logicOperator, conditionNode);
        joinNode.setCondition(groupNode);

        return this;
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
