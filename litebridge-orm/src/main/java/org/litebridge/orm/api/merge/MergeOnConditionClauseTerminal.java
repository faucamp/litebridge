package org.litebridge.orm.api.merge;

import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.api.select.ConditionClauseTerminal;
import org.litebridge.orm.api.select.ast.MergeNode;
import org.litebridge.orm.api.select.ast.UsingNode;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;

public final class MergeOnConditionClauseTerminal<DTO, MUS extends MergeUpdateStep<DTO>>

        extends MergeWhenMatchedStep<DTO, MUS>

        implements ConditionClauseTerminal<DTO,
        MergeConditionClause<DTO, MUS, MergeOnConditionClauseTerminal<DTO, MUS>>,
        MergeOnConditionClauseTerminal<DTO, MUS>> {

    public MergeOnConditionClauseTerminal(final MergeNode mergeNode,
                                          final UsingNode usingNode,
                                          final LitebridgeContext litebridgeContext) {
        super(mergeNode, usingNode.table(), usingNode, litebridgeContext);

    }

    @Override
    public MergeConditionClause<DTO, MUS, MergeOnConditionClauseTerminal<DTO, MUS>> and(final String column) {
        return null;
    }

    @Override
    public MergeConditionClause<DTO, MUS, MergeOnConditionClauseTerminal<DTO, MUS>> and(final ExpressionSpec expression) {
        return null;
    }

    @Override
    public MergeOnConditionClauseTerminal<DTO, MUS> and(final QueryConditionBuilder<DTO> query) {
        return null;
    }

    @Override
    public MergeConditionClause<DTO, MUS, MergeOnConditionClauseTerminal<DTO, MUS>> or(final String column) {
        return null;
    }

    @Override
    public MergeConditionClause<DTO, MUS, MergeOnConditionClauseTerminal<DTO, MUS>> or(final ExpressionSpec expression) {
        return null;
    }

    @Override
    public MergeOnConditionClauseTerminal<DTO, MUS> or(final QueryConditionBuilder<DTO> query) {
        return null;
    }
}
