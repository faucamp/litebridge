package org.litebridge.orm.api.spec;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Specification of a database column, used to map DTO fields to target columns.
 * <p>
 * This class is immutable and provides various factory methods to create instances
 * with different configurations.
 *
 * @param name            Database column name
 * @param isAutoIncrement Whether column is set to auto-increment
 * @param sequence        Name of the sequence used to generate values for this column
 * @param joinColumn      Field name of the nested DTO to join on
 *
 */
@NullMarked
public record ColumnSpec(
        String name,
        boolean isAutoIncrement,
        @Nullable
        String sequence,
        @Nullable
        String joinColumn,
        @Nullable
        TableMapping mappedTable) implements ColumnMapping {

    public ColumnSpec(final String name,
                      final boolean isAutoIncrement,
                      final @Nullable String sequence,
                      final @Nullable String joinColumn) {
        this(name, isAutoIncrement, sequence, joinColumn, null);
    }
}