package org.litebridge.db.spi.update;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.ColumnMetaData;

/**
 * A value associated with a specific column in a database operation.
 * <p>
 * This record combines:
 * - The {@link ColumnMetaData} which defines the metadata of the column.
 * - The value associated with the column, which may be null.
 * <p>
 * Used in database operations such as INSERT and UPDATE to pair column metadata
 * with its corresponding value. Instances of this record are immutable, ensuring
 * thread safety and reliability in database operations.
 *
 * @param column The metadata defining the column.
 * @param value  The value associated with the column, which may be null.
 */
public record ColumnValue(ColumnMetaData column, @Nullable Object value) {
}
