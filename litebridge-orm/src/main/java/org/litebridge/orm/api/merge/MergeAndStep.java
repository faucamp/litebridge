package org.litebridge.orm.api.merge;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;

public sealed class MergeAndStep<DTO, MUS extends MergeUpdateStep<DTO>>
        extends MergeStepBase
        permits MergeWhenMatchedConditionClauseTerminal {

    private final QueryNode node;
    private final LitebridgeContext litebridgeContext;

    public MergeAndStep(final String targetTable, final String usingTable, final QueryNode node, final LitebridgeContext litebridgeContext) {
        super(targetTable, usingTable, litebridgeContext);
        this.node = node;
        this.litebridgeContext = litebridgeContext;
    }

    public MergeConditionClause<DTO, MUS, MergeWhenMatchedConditionClauseTerminal<DTO, MUS>> and(final String column) {
        return andImpl(column, null);
    }

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

    QueryNode node() {
        return node;
    }
}
