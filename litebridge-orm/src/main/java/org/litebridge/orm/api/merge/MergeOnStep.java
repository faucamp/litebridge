package org.litebridge.orm.api.merge;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.delete.DtoDeleteWhereConditionClause;
import org.litebridge.orm.api.select.ast.MergeNode;
import org.litebridge.orm.api.select.ast.UsingNode;
import org.litebridge.orm.engine.LitebridgeContext;
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

    DtoDeleteWhereConditionClause<DTO> whereImpl(final LogicOperator logicOperator, final String field) {
//        final Column column = deleteSpec.dtoTable().getColumnForFieldName(field).toColumn();
//        return whereImpl(logicOperator, new SelectColumnSpec(column));
        throw new UnsupportedOperationException("Not implemented yet");
    }

    DtoDeleteWhereConditionClause<DTO> whereImpl(final LogicOperator logicOperator, final ExpressionSpec expression) {
//        final Function<QueryNode, DtoDeleteWhereConditionClauseTerminal<DTO>> recreator = n -> {
//            this.node = new WhereNode(this.node, n);
//            return new DtoDeleteWhereConditionClauseTerminalImpl<>(this);
//        };
//        return new DtoDeleteWhereConditionClause<>(litebridgeContext, logicOperator, expression, recreator);
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
