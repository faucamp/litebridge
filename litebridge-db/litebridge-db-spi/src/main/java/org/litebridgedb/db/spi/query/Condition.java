package org.litebridgedb.db.spi.query;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.db.spi.expression.LiteralExpression;
import org.litebridgedb.db.spi.expression.SelectExpression;

/**
 * A condition in a database query, specifying a column, operator, and value/operand.
 *
 * @param lhs      Left-hand side of the condition; usually a expression expression.
 * @param operator Operator for this condition, which is used to define the comparison type in a query (e.g., equality, greater than, less than).
 * @param rhs      Right-hand side/operand/value associated with the condition.
 * @see Operator
 * @see Join
 */
public record Condition(SelectExpression lhs, Operator operator, @Nullable SelectExpression rhs) {

    /**
     * Convenience constructor that wraps the given value into a {@link LiteralExpression}.
     * <p>
     * Equivalent to {@code Condition(lhs, operator, new LiteralExpression(value))}.
     *
     * @param lhs      Left-hand side of the condition; usually a expression expression.
     * @param operator {@code IS_NULL} or {@code IS_NOT_NULL} operator
     * @param value    Literal value/operand for the RHS of the condition; may be {@code null}.
     * @throws IllegalArgumentException if {@code operator} is not {@code IS_NULL} or {@code IS_NOT_NULL}
     */
    public Condition(final SelectExpression lhs, final Operator operator, final @Nullable Object value) {
        this(lhs, operator, new LiteralExpression(value));
    }

    /**
     * Convenience constructor for {@code Operator.IS_NULL} and @{code Operator.IS_NOT_NULL} operators.
     * <p>
     * Equivalent to {@code Condition(lhs, operator, null)}.
     *
     * @param lhs      Left-hand side of the condition; usually a expression expression.
     * @param operator {@code IS_NULL} or {@code IS_NOT_NULL} operator
     * @throws IllegalArgumentException if {@code operator} is not {@code IS_NULL} or {@code IS_NOT_NULL}
     */
    public Condition(final SelectExpression lhs, final Operator operator) {
        this(lhs, operator, null);

        if (operator != Operator.IS_NULL && operator != Operator.IS_NOT_NULL) {
            throw new IllegalArgumentException("Operator must be IS_NULL or IS_NOT_NULL");
        }
    }
}
