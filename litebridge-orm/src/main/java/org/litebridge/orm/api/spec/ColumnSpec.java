package org.litebridge.orm.api.spec;

import org.jspecify.annotations.Nullable;

/**
 * Specification of a database column, used in mapping DTO fields to target columns.
 * <p>
 * {@code ColumnSpec} is an abstraction used to define column-level configurations within a database table's schema.
 * <p>
 * Implementations of this interface provide details about the characteristics and behavior of a column,
 * such as its name, whether it auto-increments, the associated sequence (if any), and any join relation.
 * <p>
 * Methods in this interface allow access to these column details and support the construction of
 * immutable column definitions through builder implementations.
 */
public interface ColumnSpec {

    /**
     * Retrieves the name of the database column.
     *
     * @return the name of the column
     */
    String name();

    /**
     * Indicates whether the database column is set to auto-increment.
     *
     * @return {@code true} if the column is configured as auto-increment; otherwise, {@code false}
     */
    boolean autoIncrement();

    /**
     * Retrieves the name of the sequence associated with the database column, if any.
     * The sequence, if defined, is used to generate values for the column, typically
     * in cases where the column is configured to auto-increment.
     *
     * @return the name of the sequence associated with the column, or {@code null} if no sequence is defined
     */
    @Nullable
    String sequence();

    @Nullable
    String joinColumn();
}
