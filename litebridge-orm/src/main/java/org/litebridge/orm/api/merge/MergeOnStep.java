package org.litebridge.orm.api.merge;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.MergeNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.expression.Aliasable;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.select.ValuesSpec;

/**
 * Merge step for setting up the {@code MERGE INTO ... USING ... ON} condition.
 *
 * @param <DTO> the type of the DTO
 * @param <MUS> operation mode-specific {@code WHEN MATCHED} update step
 * @param <MIS> operation mode-specific {@code WHEN NOT MATCHED} insert step
 */
public abstract sealed class MergeOnStep<DTO,
        MCCT extends MergeOnConditionClauseTerminal<DTO, MCCT, MUS, MIS, QCB>,
        MUS extends MergeUpdateStep,
        MIS extends MergeInsertStep,
        QCB extends QueryConditionBuilder<DTO, ?, ?, ?, QCB>>

        extends MergeStepBase
        permits DtoMergeOnStep, SqlMergeOnStep {

    protected final @Nullable String usingAlias;

    /**
     * Creates a new {@code MergeOnStep} instance for SQL mode.
     *
     * @param usingTable        the merge using table
     * @param mergeNode         the root merge node
     * @param litebridgeContext Litebridge context
     */
    public MergeOnStep(final String usingTable,
                       final @Nullable String usingAlias,
                       final MergeNode mergeNode,
                       final LitebridgeContext litebridgeContext) {
        super(usingTable, mergeNode, litebridgeContext);
        this.usingAlias = usingAlias;
    }

    /**
     * Creates a new {@code MergeOnStep} instance.
     *
     * @param subselectNode     Subquery to merge on.
     * @param mergeNode         the root merge node
     * @param litebridgeContext the Litebridge context
     */
    public MergeOnStep(final QueryNode subselectNode,
                       final @Nullable String usingAlias,
                       final MergeNode mergeNode,
                       final LitebridgeContext litebridgeContext) {
        super(subselectNode, mergeNode, litebridgeContext);
        this.usingAlias = usingAlias;
    }

    /**
     * Creates a new {@code MergeOnStep} instance for DTO mode.
     *
     * @param usingDtoClass     the using DTO class
     * @param mergeNode         the root merge node
     * @param litebridgeContext the Litebridge context
     */
    protected MergeOnStep(final Class<?> usingDtoClass,
                          final @Nullable String usingAlias,
                          final MergeNode mergeNode,
                          final LitebridgeContext litebridgeContext) {
        super(usingDtoClass, mergeNode, litebridgeContext);
        this.usingAlias = usingAlias;
    }

    /**
     * Creates a new {@code MergeOnStep} instance.
     *
     * @param valuesSpec        Values from target specification
     * @param mergeNode         the root merge node
     * @param litebridgeContext the Litebridge context
     */
    protected MergeOnStep(final ValuesSpec valuesSpec,
                          final MergeNode mergeNode,
                          final LitebridgeContext litebridgeContext) {
        super(valuesSpec, mergeNode, litebridgeContext);
        this.usingAlias = valuesSpec.tableAlias();
    }

    /**
     * Creates a {@code MERGE INTO ... USING ... ON} condition targeting the specified column.
     *
     * @param column the LHS column of the {@code ON} condition
     * @return the next step in the update operation: setting the value of the target column
     */
    public MergeConditionClause<DTO, MUS, MCCT, QCB> on(final String column) {
        return onImpl(column, null);
    }

    /**
     * Creates a {@code MERGE INTO ... USING ... ON} condition using an expression.
     *
     * @param expression expression specifying the target LHS column of the {@code ON} condition
     * @return the next step in the update operation: setting the value of the target column
     */
    public MergeConditionClause<DTO, MUS, MCCT, QCB> on(final ExpressionSpec expression) {
        return onImpl(null, expression);
    }

    /**
     * Creates a {@code MERGE INTO ... USING ... ON} condition using a nested condition block.
     *
     * @param query the query condition builder
     * @return the condition clause terminal
     */
    public abstract MCCT on(final QCB query);

    protected abstract MCCT createMergeOnConditionClauseTerminal(final QueryNode conditionNode, final String alias);

    private MergeConditionClause<DTO, MUS, MCCT, QCB> onImpl(final @Nullable String column, final @Nullable ExpressionSpec expression) {
        final String alias;

        if (expression instanceof Aliasable aliasable && aliasable.getAlias() != null) {
            alias = aliasable.getAlias();
        } else {
            alias = usingAlias;
        }

        return new MergeConditionClause<>(litebridgeContext,
                LogicOperator.NOOP,
                column,
                expression,
                null,
                conditionNode -> createMergeOnConditionClauseTerminal(conditionNode, alias));
    }
}
