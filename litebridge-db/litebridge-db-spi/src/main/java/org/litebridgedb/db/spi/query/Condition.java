package org.litebridgedb.db.spi.query;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.db.spi.Column;

/**
 * A condition in a database query, specifying a column, operator, and value/operand.
 *
 * @param column   Name of the column associated with this condition.
 * @param operator Operator for this condition, which is used to define the comparison type in a query (e.g., equality, greater than, less than).
 * @param value    Operand associated with the condition.
 * @see Operator
 * @see Join
 */
public record Condition(Column column, Operator operator, @Nullable Object value) {

    /**
     * Convenience constructor for {@code Operator.IS_NULL} and @{code Operator.IS_NOT_NULL} operators.
     * <p>
     * Equivalent to {@code Condition(column, operator, null)}.
     *
     * @param column   Name of the column associated with this condition.
     * @param operator {@code IS_NULL} or {@code IS_NOT_NULL} operator
     * @throws IllegalArgumentException if {@code operator} is not {@code IS_NULL} or {@code IS_NOT_NULL}
     */
    public Condition(final Column column, final Operator operator) {
        this(column, operator, null);

        if (operator != Operator.IS_NULL && operator != Operator.IS_NOT_NULL) {
            throw new IllegalArgumentException("Operator must be IS_NULL or IS_NOT_NULL");
        }
    }
}
