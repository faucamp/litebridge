package org.litebridge.db.spi.sql;

import org.jspecify.annotations.Nullable;

/**
 * A binding value and its associated SQL data type.
 * <p>
 * This record is used to pair a value with its corresponding SQL type,
 * ensuring that the value can be appropriately converted in database operations.
 *
 * @param value       The object value to be bound, which may be null if representing a SQL NULL.
 * @param sqlDataType The integer value indicating the SQL data type of the bound value,
 *                    corresponding to values in {@link java.sql.Types}.
 */
public record BindValue(@Nullable Object value, int sqlDataType) {

    /**
     * Constructs a bind value without a specified SQL data type.
     *
     * @param value The object value to be bound, which may be null if representing a SQL NULL.
     */
    public BindValue(@Nullable Object value) {
        this(value, 0);
    }
}