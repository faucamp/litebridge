package org.litebridge.orm.api.merge;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.MergeNode;
import org.litebridge.orm.engine.ast.UsingNode;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.Objects;

/**
 * Merge step for setting up the {@code MERGE INTO ... USING ... ON} condition.
 *
 * @param <DTO> the type of the DTO
 * @param <MUS> operation mode-specific {@code WHEN MATCHED} update step
 * @param <MIS> operation mode-specific {@code WHEN NOT MATCHED} insert step
 */
public sealed class MergeOnStep<DTO, MUS extends MergeUpdateStep, MIS extends MergeInsertStep>
        extends MergeStepBase
        permits DtoMergeOnStep {

    protected final MergeNode mergeNode;

    /**
     * Creates a new {@code MergeOnStep} instance.
     *
     * @param usingTable        the merge using table
     * @param mergeNode         the root merge node
     * @param litebridgeContext Litebridge context
     */
    public MergeOnStep(final String usingTable, final MergeNode mergeNode, final LitebridgeContext litebridgeContext) {
        super(Objects.requireNonNull(mergeNode.table()), usingTable, litebridgeContext);
        this.mergeNode = mergeNode;
    }

    protected MergeOnStep(final Class<?> usingDtoClass, final MergeNode mergeNode, final LitebridgeContext litebridgeContext) {
        super(Objects.requireNonNull(mergeNode.dtoClass()), usingDtoClass, litebridgeContext);
        this.mergeNode = mergeNode;
    }

    /**
     * Creates a {@code MERGE INTO ... USING ... ON} condition targeting the specified column.
     *
     * @param column the LHS column of the {@code ON} condition
     * @return the next step in the update operation: setting the value of the target column
     */
    public MergeConditionClause<DTO, MUS, MergeOnConditionClauseTerminal<DTO, MUS, MIS>> on(final String column) {
        return onImpl(column, null);
    }

    /**
     * Creates a {@code MERGE INTO ... USING ... ON} condition using an expression.
     *
     * @param expression expression specifying the target LHS column of the {@code ON} condition
     * @return the next step in the update operation: setting the value of the target column
     */
    public MergeConditionClause<DTO, MUS, MergeOnConditionClauseTerminal<DTO, MUS, MIS>> on(final ExpressionSpec expression) {
        return onImpl(null, expression);
    }

    private MergeConditionClause<DTO, MUS, MergeOnConditionClauseTerminal<DTO, MUS, MIS>> onImpl(final @Nullable String column, final @Nullable ExpressionSpec expression) {
        return new MergeConditionClause<>(litebridgeContext,
                LogicOperator.NOOP,
                column,
                expression,
                null,
                conditionNode -> {
                    final UsingNode usingNode = new UsingNode(mergeNode, usingTable, usingDtoClass, conditionNode);
                    return new MergeOnConditionClauseTerminal<>(mergeNode, usingNode, litebridgeContext);
                });
    }
}
