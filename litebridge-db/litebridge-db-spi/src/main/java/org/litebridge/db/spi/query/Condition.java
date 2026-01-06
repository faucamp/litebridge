package org.litebridge.db.spi.query;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;

/**
 * Represents a condition in a database query, specifying a column, operator, and value/operand.
 *
 * @param column   Name of the column associated with this condition.
 * @param operator Operator for this condition, which is used to define the comparison type in a query (e.g., equality, greater than, less than).
 * @param value    Operand associated with the condition.
 */
public record Condition(Column column, Operator operator, @Nullable Object value) {

    /**
     * Convenience constructor for @{code Operator.IS_NULL} and @{code Operator.IS_NOT_NULL} operators.
     * Equivalent to @{code Condition(column, operator, null}.
     * @param column   Name of the column associated with this condition.
     * @param operator IS_NULL or IS_NOT_NULL operator
     */
    public Condition(final ColumnMetaData column, final Operator operator) {
        this(column, operator, null);

        if (operator != Operator.IS_NULL && operator != Operator.IS_NOT_NULL) {
            throw new IllegalArgumentException("Operator must be IS_NULL or IS_NOT_NULL");
        }
    }
}
