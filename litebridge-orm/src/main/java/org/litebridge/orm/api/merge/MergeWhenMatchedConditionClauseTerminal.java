package org.litebridge.orm.api.merge;

import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.api.select.ConditionClauseTerminal;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.expression.ExpressionSpec;

/**
 * Terminal step for a MERGE {@code WHEN MATCHED} condition clause.
 *
 * @param <DTO> the mapped DTO/entity type or {@link org.litebridge.db.spi.Row}
 * @param <MUS> the merge update step type
 */
public final class MergeWhenMatchedConditionClauseTerminal<DTO, MUS extends MergeUpdateStep>
        extends MergeAndStep<DTO, MUS>
        implements ConditionClauseTerminal<DTO,
        MergeConditionClause<DTO, MUS, MergeWhenMatchedConditionClauseTerminal<DTO, MUS>>,
        MergeWhenMatchedConditionClauseTerminal<DTO, MUS>> {

    /**
     * Creates a new {@code MergeWhenMatchedConditionClauseTerminal} instance.
     *
     * @param targetTable       the target table name
     * @param usingTable        the using table name
     * @param node              the current query node
     * @param litebridgeContext the Litebridge context
     */
    public MergeWhenMatchedConditionClauseTerminal(final String targetTable, final String usingTable, final QueryNode node, final LitebridgeContext litebridgeContext) {
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
