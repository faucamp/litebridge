package org.litebridge.db.spi.query;

import org.litebridge.db.spi.Column;

/**
 * Represents a condition in a database query, specifying a column, operator, and value/operand.
 *
 * @param column   Name of the column associated with this condition.
 * @param operator Operator for this condition, which is used to define the comparison type in a query (e.g., equality, greater than, less than).
 * @param value    Operand associated with the condition.
 */
public record Condition(Column column, Operator operator, Object value) {
}
