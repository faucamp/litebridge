package org.litebridge.orm.api.merge;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.MergeNode;
import org.litebridge.orm.engine.ast.UsingNode;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.Objects;

public sealed class MergeOnStep<DTO, MUS extends MergeUpdateStep<DTO>>
        extends MergeStepBase
        permits DtoMergeOnStep {

    protected final MergeNode mergeNode;

    public MergeOnStep(final String usingTable, final MergeNode mergeNode, final LitebridgeContext litebridgeContext) {
        super(Objects.requireNonNull(mergeNode.table()), usingTable, litebridgeContext);
        this.mergeNode = mergeNode;
    }

    protected MergeOnStep(final Class<?> usingDtoClass, final MergeNode mergeNode, final LitebridgeContext litebridgeContext) {
        super(Objects.requireNonNull(mergeNode.dtoClass()), usingDtoClass, litebridgeContext);
        this.mergeNode = mergeNode;
    }

    public MergeConditionClause<DTO, MUS, MergeOnConditionClauseTerminal<DTO, MUS>> on(final String column) {
        return onImpl(column, null);
    }

    public MergeConditionClause<DTO, MUS, MergeOnConditionClauseTerminal<DTO, MUS>> on(final ExpressionSpec expression) {
        return onImpl(null, expression);
    }

    private MergeConditionClause<DTO, MUS, MergeOnConditionClauseTerminal<DTO, MUS>> onImpl(final @Nullable String column, final @Nullable ExpressionSpec expression) {
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
