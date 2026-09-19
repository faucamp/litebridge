package org.litebridge.db.spi;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.ObjectUtils;
import org.litebridge.commons.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * A row of data returned from a database query.
 * <p>
 * Represents a row within a dataset, containing a collection of {@link RowColumn} objects.
 * Each column in the row can be accessed either by its index or by its label/alias.
 */
public final class Row {

    private final List<RowColumn> columns;
    private @Nullable LinkedHashMap<String, RowColumn> columnMap;

    public Row(final List<RowColumn> columns) {
        this.columns = columns;
    }

    /**
     * Returns the list of columns in this row.
     *
     * @return The list of row columns
     */
    public List<RowColumn> columns() {
        return columns;
    }

    /**
     * Provides a map of column labels to {@link RowColumn} objects for the current row.
     * <p>
     * The underlying map implementation is a {@link LinkedHashMap}, preserving the order of columns.
     * <p>
     * If the underlying map is not already initialised, it will be created and populated with
     * the columns in this row; subsequent calls will return the same map.
     *
     * @return A map where the keys are column labels (strings) and the values
     * are the corresponding {@link RowColumn} objects.
     */
    public Map<String, RowColumn> columnMap() {
        return ensureColumnMap();
    }

    /**
     * Retrieves the value of the column at the given index.
     *
     * @param index The row column index
     * @return The row column value
     * @throws IndexOutOfBoundsException if an invalid column index is specified
     */
    public @Nullable Object value(final int index) {
        return columns.get(index).value();
    }

    /**
     * Retrieves the value of the column with the given label/alias.
     *
     * @param label The row column label (column name or alias)
     * @return The row column value
     * @throws NoSuchElementException if the column is not found
     */
    public @Nullable Object value(final String label) {
        return column(label).value();
    }

    /**
     * Retrieves the column at the given index.
     *
     * @param index The row column index
     * @return The row column
     * @throws IndexOutOfBoundsException if an invalid column index is specified
     */
    public RowColumn column(final int index) throws IndexOutOfBoundsException {
        return columns.get(index);
    }

    /**
     * Retrieves the column with the given label/alias.
     *
     * @param label The row column label (column name or alias)
     * @return The row column
     * @throws NoSuchElementException if the column is not found
     */
    public RowColumn column(final String label) throws NoSuchElementException {
        return ObjectUtils.requireNonNull(ensureColumnMap().get(label),
                () -> new NoSuchElementException("Row column not found: " + label));
    }

    /**
     * Retrieves the index of a column from the row by its {@code Column} metadata.
     *
     * @param column the column metadata to match
     * @return the index of the column if found, or -1 otherwise
     */
    public int indexOf(final Column column) {
        return indexOf(column.name());
    }

    /**
     * Retrieves the index of a column from the row by its name.
     *
     * @param label the label of the column to retrieve
     * @return the index of the column if found, or -1 otherwise
     */
    public int indexOf(final String label) {
        int index = 0;

        for (RowColumn rowColumn : columns) {
            if (StringUtils.equalsIgnoreCase(rowColumn.label(), label)) {
                return index;
            }

            index++;
        }

        return -1;
    }

    /**
     * Returns the number of columns in this row.
     *
     * @return The number of columns
     */
    public int size() {
        return columns.size();
    }

    private LinkedHashMap<String, RowColumn> ensureColumnMap() {
        if (columnMap == null) {
            columnMap = new LinkedHashMap<>();

            for (RowColumn rowColumn : columns) {
                columnMap.put(rowColumn.label(), rowColumn);

                final Column column = rowColumn.column();

                // Store the column name as well as a fallback, unless it overlaps with an existing label
                if (column != null && !column.name().equals(rowColumn.label()) && !columnMap.containsKey(column.name())) {
                    columnMap.put(column.name(), rowColumn);
                }
            }
        }

        return columnMap;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o || (o instanceof final Row that
                && Objects.equals(this.columns, that.columns))) {
            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(columns);
    }

    @Override
    public String toString() {
        return columns.toString();
    }

    void updateColumn(final int columnIndex, final RowColumn rowColumn) {
        columns.set(columnIndex, rowColumn);
    }
}
