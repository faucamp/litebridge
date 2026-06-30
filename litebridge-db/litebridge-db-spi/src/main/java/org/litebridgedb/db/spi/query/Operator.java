package org.litebridgedb.db.spi.query;

/**
 * Enum of various operators that can be used in database query conditions.
 * <p>
 * The operators define the comparison or logical operations used in constructing query conditions.
 * They are primarily used in conjunction with the {@code Condition} class to define query filters.
 * <p>
 * List of supported operators:
 * <ul>
 *  <li>{@link Operator#EQ}: Equality comparison.</li>
 *  <li>{@link Operator#NEQ}: Inequality comparison.</li>
 *  <li>{@link Operator#GT}: "Greater than" comparison.</li>
 *  <li>{@link Operator#GTE}: "Greater than or equal to" comparison.</li>
 *  <li>{@link Operator#LT}: "Less than" comparison.</li>
 *  <li>{@link Operator#LTE}: "Less than or equal to" comparison.</li>
 *  <li>{@link Operator#IN}: Inclusion in a set.</li>
 *  <li>{@link Operator#NOT_IN}: Exclusion from a set.</li>
 *  <li>{@link Operator#IS_NULL}: Checks if a value is {@code null}.</li>
 *  <li>{@link Operator#IS_NOT_NULL}: Checks if a value is not {@code null}.</li>
 *  <li>{@link Operator#USING}: Used to specify expressions for joining tables.</li>
 * </ul>
 *
 * @see Condition
 */
public enum Operator {
    /**
     * Equals
     */
    EQ,
    /**
     * Not equals
     */
    NEQ,
    /**
     * Greater than
     */
    GT,
    /**
     * Greater than or equal to
     */
    GTE,
    /**
     * Less than
     */
    LT,
    /**
     * Less than or equal to
     */
    LTE,
    /**
     * Inclusion in a set
     */
    IN,
    /**
     * Exclusion from a set
     */
    NOT_IN,
    /**
     * Checks if a value is {@code null}
     */
    IS_NULL,
    /**
     * Checks if a value is not {@code null}
     */
    IS_NOT_NULL,
    /**
     * Used to specify expressions for joining tables.
     */
    USING
}