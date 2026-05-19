package org.litebridgedb.orm.api.spec;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.litebridgedb.db.spi.generator.ColumnValueGenerator;

/**
 * Specification of a database column, used to map DTO fields to target columns.
 * <p>
 * This class is immutable and provides various factory methods to create instances
 * with different configurations.
 *
 * @param name            Database column name
 * @param isAutoIncrement Whether column is set to auto-increment
 * @param generator       Generator used to create a value for this column if not specified during inserts
 * @param joinColumn      Field name of the nested DTO to join on
 * @param mappedTable     In-line mapped table specification
 *
 */
@NullMarked
public record ColumnSpec(
        String name,
        boolean isAutoIncrement,
        @Nullable
        ColumnValueGenerator generator,
        @Nullable
        String joinColumn,
        @Nullable
        TableMapping mappedTable) implements ColumnMapping {

    public ColumnSpec(final String name,
                      final boolean isAutoIncrement,
                      final @Nullable ColumnValueGenerator generator,
                      final @Nullable String joinColumn) {
        this(name, isAutoIncrement, generator, joinColumn, null);
    }

    public ColumnSpec(final String name) {
        this(name, false, null, null, null);
    }
}