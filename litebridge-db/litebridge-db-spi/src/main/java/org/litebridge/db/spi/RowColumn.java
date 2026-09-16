package org.litebridge.db.spi;

import org.jspecify.annotations.Nullable;

import java.util.StringJoiner;

/**
 * A column within a row of a database query result.
 * <p>
 * The column is defined by a label, a nullablel value, and an optional
 * reference to a {@link Column} instance. The label acts as the unique
 * identifier or alias for the column in the row, while the value holds
 * the data associated with the column at runtime.
 * <p>
 * This class is immutable and designed to be part of the {@link Row} class,
 * which represents a collection of such columns within a single row of
 * query results.
 *
 * @param label  The label or alias that identifies this column in a row; must not be null.
 * @param value  The optional value associated with this column; may be null if no value is present.
 * @param column An optional reference to the {@link Column} defining this column's metadata; can be null.
 */
public record RowColumn(String label, @Nullable Object value, @Nullable Column column) {

    @Override
    public String toString() {
        return new StringJoiner(", ", "{", "}")
                .add("label='" + label + "'")
                .add("value=" + value)
                .toString();
    }
}