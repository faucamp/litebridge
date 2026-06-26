package org.litebridgedb.db.spi.query;

import java.util.Optional;

/**
 * Pagination parameters for database queries, defining a limit on
 * the number of rows returned and an optional offset indicating the starting
 * point for the result set.
 * <p>
 * This record is typically used in query-building processes to impose
 * restrictions on the result set size, which is useful for implementing
 * pagination or controlling resource usage during query execution.
 *
 * @param limit  An optional rhs specifying the maximum number of rows to return.
 * @param offset An optional rhs specifying the starting position for the result set.
 * @see Select
 */
public record Limit(Optional<Integer> limit, Optional<Integer> offset) {
}
