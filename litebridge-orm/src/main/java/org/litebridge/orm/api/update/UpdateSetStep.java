package org.litebridge.orm.api.update;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.math.MathOperator;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.ast.SetNode;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.function.Function;

/**
 * Base step for specifying the value of a column or expression in a {@code SET} clause.
 *
 * @param <DTO>  the mapped DTO/entity type or row type
 * @param <US>   the update step type returned after setting a value
 * @param <WCC>  the where condition clause type
 * @param <WCCT> the where condition clause terminal type
 */
public abstract sealed class UpdateSetStep<DTO,
        US extends UpdateStep<DTO, WCC, WCCT>,
        WCC extends UpdateWhereConditionClause<DTO, WCC, WCCT>,
        WCCT extends UpdateWhereConditionClauseTerminal<DTO, WCC, WCCT>>

        permits DtoUpdateSetStep, SqlUpdateSetStep {

    private final @Nullable String column;
    private final @Nullable ExpressionSpec expressionSpec;
    private final QueryNode node;
    private final Function<QueryNode, US> updateStepCreator;

    /**
     * Creates a new {@code UpdateSetStep} instance with a column or field name.
     *
     * @param column            the column or field name to update
     * @param node              the current query node
     * @param updateStepCreator the function to create the update step
     */
    protected UpdateSetStep(final String column,
                            final QueryNode node,
                            final Function<QueryNode, US> updateStepCreator) {
        this(column, null, node, updateStepCreator);
    }

    /**
     * Creates a new {@code UpdateSetStep} instance with an expression specification.
     *
     * @param expressionSpec    the expression specification defining the column or target
     * @param node              the current query node
     * @param updateStepCreator the function to create the update step
     */
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

    /**
     * Sets the target column or field to the specified value.
     *
     * @param value the value to set
     * @return the next update step
     */
    public US to(final Object value) {
        return addSetNode(null, value);
    }

    /**
     * Increments the target column or field by 1.
     *
     * @return the next update step
     */
    public US increment() {
        return add(1);
    }

    /**
     * Adds the specified value to the target column or field.
     *
     * @param value the value to add
     * @return the next update step
     */
    public US add(final Object value) {
        return addSetNode(MathOperator.ADD, value);
    }

    /**
     * Subtracts the specified value from the target column or field.
     *
     * @param value the value to subtract
     * @return the next update step
     */
    public US minus(final Object value) {
        return addSetNode(MathOperator.SUBTRACT, value);
    }

    /**
     * Multiplies the target column or field by the specified value.
     *
     * @param value the value to multiply by
     * @return the next update step
     */
    public US multiply(final Object value) {
        return addSetNode(MathOperator.MULTIPLY, value);
    }

    /**
     * Divides the target column or field by the specified value.
     *
     * @param value the divisor value
     * @return the next update step
     */
    public US divide(final Object value) {
        return addSetNode(MathOperator.DIVIDE, value);
    }

    /**
     * Applies a modulo operation to the target column or field with the specified value.
     *
     * @param value the modulus value
     * @return the next update step
     */
    public US mod(final Object value) {
        return addSetNode(MathOperator.MOD, value);
    }

    private US addSetNode(final @Nullable MathOperator mathOperator, final Object value) {
        final SetNode setNode = new SetNode(node, column, expressionSpec, value, mathOperator);
        return updateStepCreator.apply(setNode);
    }
}
