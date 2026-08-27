package org.litebridge.db.spi.query;

import org.jspecify.annotations.Nullable;

/**
 * Pagination parameters for database queries, defining a limit on
 * the number of rows returned and an optional offset indicating the starting
 * point for the result set.
 * <p>
 * This record is typically used in query-building processes to impose
 * restrictions on the result set size, which is useful for implementing
 * pagination or controlling resource usage during query execution.
 *
 * @param limit  An optional value specifying the maximum number of rows to return.
 * @param offset An optional value specifying the starting position for the result set.
 * @see Select
 */
public record Limit(@Nullable Integer limit, @Nullable Integer offset) {

    public Limit(@Nullable final Integer limit, @Nullable final Integer offset) {
        if (limit == null && offset == null) {
            throw new IllegalArgumentException("Either limit or offset must be specified");
        }

        this.limit = limit;
        this.offset = offset;
    }
}
