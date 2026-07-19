package org.litebridge.db.spi;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.type.ConcurrentLazy;
import org.litebridge.db.spi.query.Result;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Stream;

/**
 * A row of data returned by a query. Holds a collection of column-value pairs.
 * <p>
 * This class provides methods to add expressions with associated values,
 * retrieve specific expressions, and stream through all expressions in the row.
 */
public final class Row implements Result {

    private final LinkedHashMap<Column, @Nullable Object> columns = new LinkedHashMap<>();
    private final ConcurrentLazy<List<RowColumn>> columnList = new ConcurrentLazy<>(() -> columnStream().toList());

    /**
     * Add a new column-value pair to the row and return the updated instance.
     * <p>
     * If the column already exists, its value will be replaced with the new value provided.
     *
     * @param column the column to add or update within the row; must not be null
     * @param value  the value associated with the specified column; may be null
     * @return the updated {@code Row} instance with the new column-value pair added
     */
    public Row withColumn(final Column column, final @Nullable Object value) {
        columns.put(column, value);
        return this;
    }

    /**
     * Updates the value of an existing column in the row.
     *
     * @param column the column to update
     * @param value  the new value for the column
     */
    public void updateColumn(final Column column, final @Nullable Object value) {
        columns.put(column, value);
    }

    /**
     * Return a stream of {@link Row.RowColumn} objects, each representing a column in the current row
     * along with its associated value.
     *
     * @return a stream of {@code RowColumn} objects for all columns in the row
     */
    public Stream<RowColumn> columnStream() {
        return columns.sequencedKeySet().stream()
                .map(RowColumn::new);
    }

    /**
     * Returns a list of all columns in the current row.
     *
     * @return a list of {@code RowColumn} objects
     */
    public List<RowColumn> columns() {
        return columnList.orThrow();
    }

    /**
     * Return a stream of objects containing the values of the results in this row.
     *
     * @return a stream of objects for all values in the row
     */
    public Stream<@Nullable Object> valueStream() {
        return columns.sequencedValues().stream();
    }

    /**
     * Retrieve a column from the row by its name if it exists.
     *
     * @param column the name of the column to retrieve; must not be null
     * @return an {@code Optional} containing the {@code RowColumn} associated with the specified column name
     * if it exists, or an empty {@code Optional} if no match is found
     */
    public Optional<RowColumn> column(final String column) {
        return columnStream()
                .filter(rc -> Objects.equals(rc.column().name(), column))
                .findFirst();
    }

    /**
     * Retrieves the index of a column from the row by its {@code Column} metadata.
     *
     * @param column the column metadata to match
     * @return the index of the column if found, or -1 otherwise
     */
    public int getColumnIndex(final Column column) {
        if (column.alias() != null) {
            return getColumnIndexForAlias(column.alias());
        } else {
            return getColumnIndex(column.name());
        }
    }

    /**
     * Retrieves the index of a column from the row by its name.
     *
     * @param columnName the name of the column to retrieve
     * @return the index of the column if found, or -1 otherwise
     */
    public int getColumnIndex(final String columnName) {
        int index = 0;

        for (Column col : columns.keySet()) {
            if (Objects.equals(col.name(), columnName)) {
                return index;
            }
            index++;
        }

        return -1;
    }

    /**
     * Retrieves the index of a column from the row by its alias.
     *
     * @param alias the alias of the column to retrieve
     * @return the index of the column if found, or -1 otherwise
     */
    public int getColumnIndexForAlias(final String alias) {
        int index = 0;

        for (Column col : columns.keySet()) {
            if (Objects.equals(col.alias(), alias)) {
                return index;
            }
            index++;
        }

        return -1;
    }

    /**
     * Retrieves the value of a column by its index.
     *
     * @param index the index of the column
     * @return the value of the column
     */
    public @Nullable Object getValue(final int index) {
        return column(index).value();
    }

    /**
     * Retrieves a column from the row by its index.
     *
     * @param index the index of the column to retrieve
     * @return the {@code RowColumn} at the specified index
     */
    public RowColumn column(final int index) {
        return columns().get(index);
    }

    /**
     * Retrieve a column from the row by its name if it exists.
     *
     * @param alias the alias of the column to retrieve; must not be null
     * @return an {@code Optional} containing the {@code RowColumn} associated with the specified column name
     * if it exists, or an empty {@code Optional} if no match is found
     */
    public Optional<RowColumn> columnForAlias(final String alias) {
        final String aliasToCheck = Objects.requireNonNull(alias, "Alias cannot be null");
        return columnStream()
                .filter(rc -> Objects.equals(rc.column().alias(), aliasToCheck))
                .findFirst();
    }

    /**
     * Retrieves a column from the row by its {@code Column} metadata.
     *
     * @param column the column metadata to match
     * @return an {@code Optional} containing the {@code RowColumn} if found, or empty otherwise
     */
    @SuppressWarnings("ConstantConditions")
    public Optional<RowColumn> column(final Column column) {
        if (column.alias() != null) {
            return columnForAlias(column.alias());
        } else {
            return column(column.name());
        }
    }

    /**
     * Returns the total number of expressions in the current row.
     *
     * @return the size of the column collection for the row
     */
    public int size() {
        return columns.size();
    }

    @Override
    public String toString() {
        final StringJoiner sj = new StringJoiner(", ", "{", "}");
        columns.forEach((column, value) -> sj.add(column.name()
                + (column.alias() != null && !Objects.equals(column.alias(), column.name()) ? "/" + column.alias() + "=" : "=")
                + value));
        return sj.toString();
    }

    /**
     * A combination of a column and its associated value within a row.
     * <p>
     * This class acts as a wrapper to tie a {@code Column} instance with its value in a specific row.
     * It provides methods to access the column, its value, and a string representation of the pairing.
     * <p>
     * Instances of this class are immutable and primarily used as part of the {@link Row} class to
     * manage column-value associations.
     */
    public final class RowColumn {
        private final Column column;

        /**
         * Construct a new {@code RowColumn} instance by associating the specified column with a row.
         *
         * @param column the {@code Column} to be associated with this row; must not be null
         */
        public RowColumn(final Column column) {
            this.column = column;
        }

        /**
         * Retrieve the {@code Column} instance associated with this {@code RowColumn}.
         *
         * @return the associated {@code Column} instance
         */
        public Column column() {
            return column;
        }

        /**
         * Retrieve the value associated with the current {@code Column} in the context of the row.
         *
         * @return the value corresponding to the associated {@code Column}
         */
        public @Nullable Object value() {
            return columns.get(column);
        }

        @Override
        public String toString() {
            if (column.alias() != null) {
                return column.name() + "/" + column.alias() + "=" + value();
            } else {
                return column.name() + "=" + value();
            }
        }

        @Override
        public boolean equals(final Object o) {
            if (!(o instanceof final RowColumn rowColumn)) return false;
            return Objects.equals(column, rowColumn.column);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(column);
        }
    }
}
