package org.litebridge.orm.api.merge;

import org.litebridge.commons.type.ConcurrentLazy;
import org.litebridge.db.spi.Table;
import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.api.select.ConditionClauseTerminal;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.function.Supplier;

public class MergeConditionClauseTerminal<DTO, MUS extends MergeUpdateStep<DTO>>

        extends MergeWhenMatchedAndStep<DTO, MUS>

        implements ConditionClauseTerminal<DTO,
        MergeConditionClause<DTO, MUS>,
        MergeConditionClauseTerminal<DTO, MUS>> {

    public MergeConditionClauseTerminal(final Table table,
                                        final QueryNode node,
                                        final Supplier<MUS> mergeUpdateStepSupplier,
                                        final LitebridgeContext litebridgeContext) {
        super(table, node, mergeUpdateStepSupplier, litebridgeContext);

    }

    @Override
    public MergeConditionClause<DTO, MUS> and(final String column) {
        return null;
    }

    @Override
    public MergeConditionClause<DTO, MUS> and(final ExpressionSpec expression) {
        return null;
    }

    @Override
    public MergeConditionClauseTerminal<DTO, MUS> and(final QueryConditionBuilder<DTO> query) {
        return null;
    }

    @Override
    public MergeConditionClause<DTO, MUS> or(final String column) {
        return null;
    }

    @Override
    public MergeConditionClause<DTO, MUS> or(final ExpressionSpec expression) {
        return null;
    }

    @Override
    public MergeConditionClauseTerminal<DTO, MUS> or(final QueryConditionBuilder<DTO> query) {
        return null;
    }
}
