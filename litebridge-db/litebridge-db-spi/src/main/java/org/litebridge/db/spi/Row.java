package org.litebridge.db.spi;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.Result;
import org.slf4j.Logger;

import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Stream;

/**
 * A row of data returned by a query. Holds a collection of column-value pairs.
 * <p>
 * This class provides methods to add columns with associated values,
 * retrieve specific columns, and stream through all columns in the row.
 */
public final class Row implements Result {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(Row.class);
    private final LinkedHashMap<Column, Object> columns = new LinkedHashMap<>();
    @Nullable
    private Column last;

    /**
     * Add a new column-value pair to the row and return the updated instance.
     * <p>
     * If the column already exists, its value will be replaced with the new value provided.
     *
     * @param column the column to add or update within the row; must not be null
     * @param value  the value associated with the specified column; may be null
     * @return the updated {@code Row} instance with the new column-value pair added
     */
    public Row withColumn(final Column column, final Object value) {
        if (column.name().equals("ID")) {
            LOGGER.info("Adding ID column with alias: {}, hash: {}", column.alias(), column.hashCode());

            if (last != null) {
                LOGGER.info("Equals previous: {}", last.equals(column));
            }

            last = column;
        }

        columns.put(column, value);
        return this;
    }

    /**
     * Return a stream of {@link Row.RowColumn} objects, each representing a column in the current row
     * along with its associated value.
     *
     * @return a stream of {@code RowColumn} objects for all columns in the row
     */
    public Stream<RowColumn> columnStream() {
        return columns.keySet().stream()
                .map(RowColumn::new);
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
     * Retrieve a column from the row by its name if it exists.
     *
     * @param alias the alias of the column to retrieve; must not be null
     * @return an {@code Optional} containing the {@code RowColumn} associated with the specified column name
     * if it exists, or an empty {@code Optional} if no match is found
     */
    public Optional<RowColumn> columnForAlias(final String alias) {
        return columnStream()
                .filter(rc -> Objects.equals(rc.column().alias(), alias))
                .findFirst();
    }

    /**
     * Returns the total number of columns in the current row.
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
        public Object value() {
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
