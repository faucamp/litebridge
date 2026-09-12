package org.litebridge.orm.api.merge;

import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.api.select.ConditionClauseTerminal;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.MergeNode;
import org.litebridge.orm.engine.ast.UsingNode;
import org.litebridge.orm.expression.ExpressionSpec;

/**
 * Terminal step for a MERGE ON condition clause.
 *
 * @param <DTO> the mapped DTO/entity type or {@link org.litebridge.db.spi.Row}
 * @param <MUS> the merge update step type
 * @param <MIS> the merge insert step type
 */
public final class MergeOnConditionClauseTerminal<DTO,
        MUS extends MergeUpdateStep,
        MIS extends MergeInsertStep>

        extends MergeWhenMatchedStep<DTO, MUS, MIS>

        implements ConditionClauseTerminal<DTO,
        MergeConditionClause<DTO, MUS, MergeOnConditionClauseTerminal<DTO, MUS, MIS>>,
        MergeOnConditionClauseTerminal<DTO, MUS, MIS>> {

    /**
     * Creates a new {@code MergeOnConditionClauseTerminal} instance.
     *
     * @param mergeNode         the root merge query node
     * @param usingNode         the using clause query node
     * @param litebridgeContext the Litebridge context
     */
    public MergeOnConditionClauseTerminal(final MergeNode mergeNode,
                                          final UsingNode usingNode,
                                          final LitebridgeContext litebridgeContext) {
        super(mergeNode, usingNode, litebridgeContext);

    }

    /**
     * Adds an AND condition with the specified column.
     *
     * @param column the column name
     * @return the condition clause
     */
    @Override
    public MergeConditionClause<DTO, MUS, MergeOnConditionClauseTerminal<DTO, MUS, MIS>> and(final String column) {
        return null;
    }

    /**
     * Adds an AND condition with the specified expression.
     *
     * @param expression the expression specification
     * @return the condition clause
     */
    @Override
    public MergeConditionClause<DTO, MUS, MergeOnConditionClauseTerminal<DTO, MUS, MIS>> and(final ExpressionSpec expression) {
        return null;
    }

    /**
     * Adds an AND nested condition block.
     *
     * @param query the query condition builder
     * @return the condition clause terminal
     */
    @Override
    public MergeOnConditionClauseTerminal<DTO, MUS, MIS> and(final QueryConditionBuilder<DTO> query) {
        return null;
    }

    /**
     * Adds an OR condition with the specified column.
     *
     * @param column the column name
     * @return the condition clause
     */
    @Override
    public MergeConditionClause<DTO, MUS, MergeOnConditionClauseTerminal<DTO, MUS, MIS>> or(final String column) {
        return null;
    }

    /**
     * Adds an OR condition with the specified expression.
     *
     * @param expression the expression specification
     * @return the condition clause
     */
    @Override
    public MergeConditionClause<DTO, MUS, MergeOnConditionClauseTerminal<DTO, MUS, MIS>> or(final ExpressionSpec expression) {
        return null;
    }

    /**
     * Adds an OR nested condition block.
     *
     * @param query the query condition builder
     * @return the condition clause terminal
     */
    @Override
    public MergeOnConditionClauseTerminal<DTO, MUS, MIS> or(final QueryConditionBuilder<DTO> query) {
        return null;
    }
}
