package org.litebridge.orm.api.merge;

import org.litebridge.db.spi.Table;
import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.api.select.ConditionClauseTerminal;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;

public final class MergeWhenMatchedConditionClauseTerminal<DTO, MUS extends MergeUpdateStep<DTO>>
        extends MergeAndStep<DTO, MUS>
        implements ConditionClauseTerminal<DTO,
        MergeConditionClause<DTO, MUS, MergeWhenMatchedConditionClauseTerminal<DTO, MUS>>,
        MergeWhenMatchedConditionClauseTerminal<DTO, MUS>> {

    public MergeWhenMatchedConditionClauseTerminal(final Table targetTable, final Table usingTable, final QueryNode node, final LitebridgeContext litebridgeContext) {
        super(targetTable, usingTable, node, litebridgeContext);
    }

    @Override
    public MergeWhenMatchedConditionClauseTerminal<DTO, MUS> and(final QueryConditionBuilder<DTO> query) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public MergeConditionClause<DTO, MUS, MergeWhenMatchedConditionClauseTerminal<DTO, MUS>> or(final String column) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public MergeConditionClause<DTO, MUS, MergeWhenMatchedConditionClauseTerminal<DTO, MUS>> or(final ExpressionSpec expression) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public MergeWhenMatchedConditionClauseTerminal<DTO, MUS> or(final QueryConditionBuilder<DTO> query) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
