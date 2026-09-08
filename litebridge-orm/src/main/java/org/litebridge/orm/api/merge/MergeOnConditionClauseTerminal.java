package org.litebridge.orm.api.merge;

import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.api.select.ConditionClauseTerminal;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.MergeNode;
import org.litebridge.orm.engine.ast.UsingNode;
import org.litebridge.orm.expression.ExpressionSpec;

public final class MergeOnConditionClauseTerminal<DTO,
        MUS extends MergeUpdateStep,
        MIS extends MergeInsertStep>

        extends MergeWhenMatchedStep<DTO, MUS, MIS>

        implements ConditionClauseTerminal<DTO,
        MergeConditionClause<DTO, MUS, MergeOnConditionClauseTerminal<DTO, MUS, MIS>>,
        MergeOnConditionClauseTerminal<DTO, MUS, MIS>> {

    public MergeOnConditionClauseTerminal(final MergeNode mergeNode,
                                          final UsingNode usingNode,
                                          final LitebridgeContext litebridgeContext) {
        super(mergeNode, usingNode, litebridgeContext);

    }

    @Override
    public MergeConditionClause<DTO, MUS, MergeOnConditionClauseTerminal<DTO, MUS, MIS>> and(final String column) {
        return null;
    }

    @Override
    public MergeConditionClause<DTO, MUS, MergeOnConditionClauseTerminal<DTO, MUS, MIS>> and(final ExpressionSpec expression) {
        return null;
    }

    @Override
    public MergeOnConditionClauseTerminal<DTO, MUS, MIS> and(final QueryConditionBuilder<DTO> query) {
        return null;
    }

    @Override
    public MergeConditionClause<DTO, MUS, MergeOnConditionClauseTerminal<DTO, MUS, MIS>> or(final String column) {
        return null;
    }

    @Override
    public MergeConditionClause<DTO, MUS, MergeOnConditionClauseTerminal<DTO, MUS, MIS>> or(final ExpressionSpec expression) {
        return null;
    }

    @Override
    public MergeOnConditionClauseTerminal<DTO, MUS, MIS> or(final QueryConditionBuilder<DTO> query) {
        return null;
    }
}
