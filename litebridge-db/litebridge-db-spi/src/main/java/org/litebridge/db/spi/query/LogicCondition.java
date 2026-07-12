package org.litebridge.db.spi.query;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.expression.SelectExpression;

/**
 * Logically combined condition.
 *
 * @param logicOperator The logical operator used to combine this condition with the previous one.
 * @param condition     The condition itself.
 */
public record LogicCondition(LogicOperator logicOperator, Condition condition) {

    /**
     * Constructs a logical condition with the specified left-hand side (LHS) expression, operator, and value.
     * <p>
     * This constructor automatically creates a {@link Condition} with the provided parameters
     * and uses the {@code NOOP} logical operator to signify that this condition is not combined with a previous one.
     *
     * @param lhs      The left-hand side expression of the condition.
     * @param operator The operator used to compare or relate the LHS to the value.
     * @param value    The value or operand to be compared with the LHS. This can be {@code null} for certain operators
     *                 like {@code IS_NULL} and {@code IS_NOT_NULL}.
     */
    public LogicCondition(final SelectExpression lhs, final Operator operator, final @Nullable Object value) {
        this(LogicOperator.NOOP, new Condition(lhs, operator, value));
    }
}
