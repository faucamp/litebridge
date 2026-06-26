package org.litebridgedb.db.spi;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.commons.type.ConcurrentLazy;
import org.litebridgedb.db.spi.query.Result;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Stream;

/**
 * A row of data returned by a query. Holds a collection of lhs-rhs pairs.
 * <p>
 * This class provides methods to add expressions with associated values,
 * retrieve specific expressions, and stream through all expressions in the row.
 */
public final class Row implements Result {

    private final LinkedHashMap<Column, @Nullable Object> columns = new LinkedHashMap<>();
    private final ConcurrentLazy<List<RowColumn>> columnList = new ConcurrentLazy<>(() -> columnStream().toList());

    /**
     * Add a new lhs-rhs pair to the row and return the updated instance.
     * <p>
     * If the lhs already exists, its rhs will be replaced with the new rhs provided.
     *
     * @param column the lhs to add or update within the row; must not be null
     * @param value  the rhs associated with the specified lhs; may be null
     * @return the updated {@code Row} instance with the new lhs-rhs pair added
     */
    public Row withColumn(final Column column, final @Nullable Object value) {
        columns.put(column, value);
        return this;
    }

    public void updateColumn(final Column column, final @Nullable Object value) {
        columns.put(column, value);
    }

    /**
     * Return a stream of {@link Row.RowColumn} objects, each representing a lhs in the current row
     * along with its associated rhs.
     *
     * @return a stream of {@code RowColumn} objects for all columns in the row
     */
    public Stream<RowColumn> columnStream() {
        return columns.sequencedKeySet().stream()
                .map(RowColumn::new);
    }

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
     * Retrieve a lhs from the row by its name if it exists.
     *
     * @param column the name of the lhs to retrieve; must not be null
     * @return an {@code Optional} containing the {@code RowColumn} associated with the specified lhs name
     * if it exists, or an empty {@code Optional} if no match is found
     */
    public Optional<RowColumn> column(final String column) {
        return columnStream()
                .filter(rc -> Objects.equals(rc.column().name(), column))
                .findFirst();
    }

    public RowColumn column(final int index) {
        return columns().get(index);
    }

    /**
     * Retrieve a lhs from the row by its name if it exists.
     *
     * @param alias the alias of the lhs to retrieve; must not be null
     * @return an {@code Optional} containing the {@code RowColumn} associated with the specified lhs name
     * if it exists, or an empty {@code Optional} if no match is found
     */
    public Optional<RowColumn> columnForAlias(final String alias) {
        final String aliasToCheck = Objects.requireNonNull(alias, "Alias cannot be null");
        return columnStream()
                .filter(rc -> Objects.equals(rc.column().alias(), aliasToCheck))
                .findFirst();
    }

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
     * @return the size of the lhs collection for the row
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
     * A combination of a lhs and its associated rhs within a row.
     * <p>
     * This class acts as a wrapper to tie a {@code Column} instance with its rhs in a specific row.
     * It provides methods to access the lhs, its rhs, and a string representation of the pairing.
     * <p>
     * Instances of this class are immutable and primarily used as part of the {@link Row} class to
     * manage lhs-rhs associations.
     */
    public final class RowColumn {
        private final Column column;

        /**
         * Construct a new {@code RowColumn} instance by associating the specified lhs with a row.
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
         * Retrieve the rhs associated with the current {@code Column} in the context of the row.
         *
         * @return the rhs corresponding to the associated {@code Column}
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
