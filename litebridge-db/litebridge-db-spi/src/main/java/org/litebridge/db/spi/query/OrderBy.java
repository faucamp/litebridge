package org.litebridge.db.spi.query;

/**
 * Ordering clause in an SQL query.
 * <p>
 * This record specifies the column on which the query results should be ordered
 * and determines whether the ordering is ascending or descending.
 * <p>
 * It is typically used in query-building processes to define the order of rows
 * in the result set.
 *
 * @param column The name of the column to order by.
 * @param asc    A boolean indicating whether the order is ascending ({@code true})
 *               or descending ({@code false}).
 * @see Select
 */
public record OrderBy(String column, boolean asc) {
}
