package org.litebridge.orm.api.update;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.math.MathOperation;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.ast.SetNode;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.function.Function;

public abstract class UpdateSetStep<DTO,
        US extends UpdateStep<DTO, WCC, WCCT>,
        WCC extends UpdateWhereConditionClause<DTO, WCC, WCCT>,
        WCCT extends UpdateWhereConditionClauseTerminal<DTO, WCC, WCCT>> {

    private final @Nullable String column;
    private final @Nullable ExpressionSpec expressionSpec;
    private final QueryNode node;
    private final Function<QueryNode, US> updateStepCreator;

    protected UpdateSetStep(final String column,
                            final QueryNode node,
                            final Function<QueryNode, US> updateStepCreator) {
        this(column, null, node, updateStepCreator);
    }

    protected UpdateSetStep(final ExpressionSpec expressionSpec,
                            final QueryNode node,
                            final Function<QueryNode, US> updateStepCreator) {
        this(null, expressionSpec, node, updateStepCreator);
    }

    private UpdateSetStep(final @Nullable String column,
                          final @Nullable ExpressionSpec expressionSpec,
                          final QueryNode node,
                          final Function<QueryNode, US> updateStepCreator) {
        this.column = column;
        this.expressionSpec = expressionSpec;
        this.node = node;
        this.updateStepCreator = updateStepCreator;
    }

    public US to(final Object value) {
        return addSetNode(value);
    }

    public US increment() {
        return add(1);
    }

    public US add(final Object value) {
        return addSetNode(new MathOperation(MathOperation.Operator.ADD, value));
    }

    public US minus(final Object value) {
        return addSetNode(new MathOperation(MathOperation.Operator.SUBTRACT, value));
    }

    public US multiply(final Object value) {
        return addSetNode(new MathOperation(MathOperation.Operator.MULTIPLY, value));
    }

    public US divide(final Object value) {
        return addSetNode(new MathOperation(MathOperation.Operator.DIVIDE, value));
    }

    public US mod(final Object value) {
        return addSetNode(new MathOperation(MathOperation.Operator.MOD, value));
    }

    private US addSetNode(final Object value) {
        final SetNode setNode = new SetNode(node, column, expressionSpec, value, true);
        return updateStepCreator.apply(setNode);
    }
}
