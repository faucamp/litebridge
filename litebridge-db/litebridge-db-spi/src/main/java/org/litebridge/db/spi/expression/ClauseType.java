package org.litebridge.db.spi.expression;

/**
 * Represents the different types of SQL clauses where an expression can be used.
 */
public enum ClauseType {
    /**
     * The SELECT clause.
     */
    SELECT,
    /**
     * A JOIN clause.
     */
    JOIN,
    /**
     * The WHERE clause.
     */
    WHERE,
    /**
     * The GROUP BY clause.
     */
    GROUP_BY,
    /**
     * The HAVING clause.
     */
    HAVING,
    /**
     * The ORDER BY clause.
     */
    ORDER_BY;
}
