package org.litebridge.orm.api.merge;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.expression.ExpressionSpec;

/**
 * Step for specifying additional AND conditions in a MERGE statement.
 *
 * @param <DTO> the mapped DTO/entity type or {@link org.litebridge.db.spi.Row}
 * @param <MUS> the merge update step type
 */
public sealed class MergeAndStep<DTO, MUS extends MergeUpdateStep>
        extends MergeStepBase
        permits MergeWhenMatchedConditionClauseTerminal {

    private final QueryNode node;
    private final LitebridgeContext litebridgeContext;

    /**
     * Creates a new {@code MergeAndStep} instance.
     *
     * @param targetTable       the target table name
     * @param usingTable        the using table name
     * @param node              the current query node
     * @param litebridgeContext the Litebridge context
     */
    public MergeAndStep(final String targetTable, final String usingTable, final QueryNode node, final LitebridgeContext litebridgeContext) {
        super(targetTable, usingTable, litebridgeContext);
        this.node = node;
        this.litebridgeContext = litebridgeContext;
    }

    /**
     * Adds an AND condition with the specified column.
     *
     * @param column the column name
     * @return the condition clause
     */
    public MergeConditionClause<DTO, MUS, MergeWhenMatchedConditionClauseTerminal<DTO, MUS>> and(final String column) {
        return andImpl(column, null);
    }

    /**
     * Adds an AND condition with the specified expression.
     *
     * @param expression the expression specification
     * @return the condition clause
     */
    public MergeConditionClause<DTO, MUS, MergeWhenMatchedConditionClauseTerminal<DTO, MUS>> and(final ExpressionSpec expression) {
        return andImpl(null, expression);
    }

    private @NonNull MergeConditionClause<DTO, MUS, MergeWhenMatchedConditionClauseTerminal<DTO, MUS>> andImpl(final @Nullable String column, final @Nullable ExpressionSpec expression) {
        return new MergeConditionClause<>(litebridgeContext,
                LogicOperator.NOOP,
                column,
                expression,
                node,
                conditionNode -> new MergeWhenMatchedConditionClauseTerminal<>(targetTable, usingTable, conditionNode, litebridgeContext));
    }
}
