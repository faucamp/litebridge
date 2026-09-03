package org.litebridge.orm.api.dto;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.api.select.HavingConditionClauseTerminal;
import org.litebridge.orm.api.select.impl.AbstractHavingClauseTerminal;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.SelectEngineTerminal;
import org.litebridge.orm.engine.ast.HavingNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.expression.ExpressionSpec;

/**
 * Terminal clause for DTO HAVING conditions.
 *
 * @param <DTO> the type of the DTO
 */
public final class DtoHavingConditionClauseTerminal<DTO>
        extends AbstractHavingClauseTerminal<DTO,
        DtoOrderByClause<DTO>,
        DtoOrderByClauseChain<DTO>>

        implements HavingConditionClauseTerminal<DTO,
        DtoHavingConditionClause<DTO>,
        DtoHavingConditionClauseTerminal<DTO>,
        DtoOrderByClause<DTO>,
        DtoOrderByClauseChain<DTO>> {

    /**
     * Creates a new DtoHavingConditionClauseTerminal.
     *
     */
    public DtoHavingConditionClauseTerminal(final QueryNode node, final SelectEngineTerminal selectEngineTerminal, final LitebridgeContext litebridgeContext) {
        super(node, selectEngineTerminal, litebridgeContext);
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
        return havingImpl(LogicOperator.AND, field, null);
    }

    @Override
    public DtoHavingConditionClause<DTO> and(final ExpressionSpec expression) {
        return havingImpl(LogicOperator.AND, null, expression);
    }

    @Override
    public DtoHavingConditionClauseTerminal<DTO> and(final QueryConditionBuilder<DTO> query) {
        return havingImpl(LogicOperator.AND, query);
    }

    @Override
    public DtoHavingConditionClause<DTO> or(final String field) {
        return havingImpl(LogicOperator.OR, field, null);
    }

    @Override
    public DtoHavingConditionClause<DTO> or(final ExpressionSpec expression) {
        return havingImpl(LogicOperator.OR, null, expression);
    }

    @Override
    public DtoHavingConditionClauseTerminal<DTO> or(final QueryConditionBuilder<DTO> query) {
        return havingImpl(LogicOperator.OR, query);
    }

    @Override
    public DtoOrderByClause<DTO> orderBy(final String... fields) {
        return new DtoOrderByClause<>(fields, node, selectEngineTerminal, litebridgeContext);
    }

    @Override
    public DtoOrderByClause<DTO> orderBy(final ExpressionSpec... fields) {
        return new DtoOrderByClause<>(fields, node, selectEngineTerminal, litebridgeContext);
    }

    private DtoHavingConditionClause<DTO> havingImpl(final LogicOperator logicOperator, final @Nullable String field, final @Nullable ExpressionSpec expression) {
        if (node instanceof HavingNode havingNode) {
            return new DtoHavingConditionClause<>(litebridgeContext,
                    logicOperator,
                    field,
                    expression,
                    havingNode.condition(),
                    conditionNode -> new DtoHavingConditionClauseTerminal<>(havingNode.withCondition(conditionNode), selectEngineTerminal, litebridgeContext));
        }

        return new DtoHavingConditionClause<>(litebridgeContext,
                logicOperator,
                field,
                expression,
                null,
                conditionNode -> new DtoHavingConditionClauseTerminal<>(new HavingNode(this.node, conditionNode), selectEngineTerminal, litebridgeContext));
    }

    private DtoHavingConditionClauseTerminal<DTO> havingImpl(final LogicOperator logicOperator, final QueryConditionBuilder<DTO> query) {
//        final DtoConditionClauseStart<DTO> conditionClauseStart = new DtoConditionClauseStart<>(ormTable, delegate.litebridgeContext().fromClauseEngine(), null);
//        final org.litebridge.orm.api.condition.AbstractCbConditionClauseTerminal<DTO> terminal = query.apply(conditionClauseStart);
//        final QueryNode conditionNode = terminal.node();
//
//        if (delegate.node() instanceof HavingNode havingNode) {
//            final ConditionGroupNode groupNode = new ConditionGroupNode(havingNode.condition(), logicOperator, conditionNode);
//            havingNode.withCondition(groupNode);
//            return this;
//        }
//
//        final ConditionGroupNode groupNode = new ConditionGroupNode(null, logicOperator, conditionNode);
//        delegate.withNode(new HavingNode(delegate.node(), groupNode));
//
//        return this;
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
