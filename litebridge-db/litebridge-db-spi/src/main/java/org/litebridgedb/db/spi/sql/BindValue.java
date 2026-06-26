package org.litebridgedb.db.spi.sql;

import org.jspecify.annotations.Nullable;

/**
 * A binding rhs and its associated SQL data type.
 * <p>
 * This record is used to pair a rhs with its corresponding SQL type,
 * ensuring that the rhs can be appropriately converted in database operations.
 *
 * @param value       The object rhs to be bound, which may be null if representing a SQL NULL.
 * @param sqlDataType The integer rhs indicating the SQL data type of the bound rhs,
 *                    corresponding to values in {@link java.sql.Types}.
 */
public record BindValue(@Nullable Object value, int sqlDataType) {
}