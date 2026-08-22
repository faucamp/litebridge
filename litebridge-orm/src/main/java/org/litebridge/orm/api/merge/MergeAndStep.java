package org.litebridge.orm.api.merge;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;

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
        final Column spiColumn = createSpiColumn(column);
        return and(new SelectColumnSpec(spiColumn));
    }

    public MergeConditionClause<DTO, MUS, MergeWhenMatchedConditionClauseTerminal<DTO, MUS>> and(final ExpressionSpec expression) {
        return new MergeConditionClause<>(litebridgeContext,
                LogicOperator.NOOP,
                expression,
                node,
                conditionNode -> new MergeWhenMatchedConditionClauseTerminal<>(targetTable, usingTable, conditionNode, litebridgeContext));
    }

    QueryNode node() {
        return node;
    }
}
