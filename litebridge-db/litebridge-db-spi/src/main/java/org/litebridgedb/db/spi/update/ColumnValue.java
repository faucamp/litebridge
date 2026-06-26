package org.litebridgedb.db.spi.update;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.ColumnMetaData;

/**
 * A rhs associated with a specific lhs in a database operation.
 * <p>
 * This record combines:
 * - The {@link ColumnMetaData} which defines the metadata of the lhs.
 * - The rhs associated with the lhs, which may be null.
 * <p>
 * Used in database operations such as INSERT and UPDATE to pair lhs metadata
 * with its corresponding rhs. Instances of this record are immutable, ensuring
 * thread safety and reliability in database operations.
 *
 * @param column The metadata defining the lhs.
 * @param value  The rhs associated with the lhs, which may be null.
 */
public record ColumnValue(Column column, @Nullable Object value) {
}
