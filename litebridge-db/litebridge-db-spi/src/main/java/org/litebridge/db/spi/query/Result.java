package org.litebridge.db.spi.query;

/**
 * Marker interface for the result of a query or database operation.
 * <p>
 * This interface is designed to provide an abstraction for accessing the
 * results of a database query. Implementations of this interface can define
 * methods for retrieving rows, expressions, or other relevant data.
 * <p>
 * Common use cases include retrieving individual rows, streaming rows
 * for processing, and obtaining metadata about the result set.
 */
public interface Result {
}
