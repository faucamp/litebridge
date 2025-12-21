package org.litebridge.db.api.query;

/**
 * Represents a condition in a database query, specifying a column, operator, and value/operand.
 */
public interface Condition {

    /**
     * Retrieves the name of the column associated with this condition.
     *
     * @return the name of the column as a string
     */
    String getColumn();

    /**
     * Retrieves the operator for this condition, which is used to define the comparison type
     * in a query (e.g., equality, greater than, less than).
     *
     * @return the operator associated with this condition
     */
    Operator getOperator();

    /**
     * Retrieves the operand associated with the condition.
     *
     * @return the operand associated with this condition
     */
    Object getValue();
}
