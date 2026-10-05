package org.litebridge.db.spi;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.ObjectUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Metadata for a database table, including its primary keys and expressions.
 * <p>
 * It extends the {@link Table} class and provides additional information about the table's expressions,
 * primary key, and column mappings.
 * <p>
 * This class is immutable and thread-safe.
 */
public sealed class TableMetaData permits VirtualTableMetaData {

    protected final Table table;
    private final List<ColumnMetaData> primaryKey;
    protected final List<ColumnMetaData> columns;
    protected final Map<String, ColumnMetaData> columnMap;

    /**
     * Construct a {@code TableMetaData} instance using the provided table, primary key, and column metadata.
     *
     * @param table      the {@code Table} object representing the database table; must not be {@code null}
     * @param primaryKey a list of column names representing the primary key of the table; must not be {@code null}
     * @param columns    a list of {@code ColumnMetaData} objects representing the expressions of the table; must not be {@code null}
     * @throws IllegalArgumentException if any primary key column metadata is not found in the provided column metadata
     */
    public TableMetaData(final Table table, final List<String> primaryKey, final List<ColumnMetaData> columns) {
        this.table = table;
        this.columns = columns;
        this.columnMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        this.columnMap.putAll(columns.stream()
                .collect(Collectors.toMap(ColumnMetaData::name,
                        Function.identity())));
        final List<String> primaryKeyNames = primaryKey.stream().map(String::toLowerCase).toList();
        this.primaryKey = columns.stream()
                .filter(column -> primaryKeyNames.contains(column.name().toLowerCase()))
                .toList();

        if (this.primaryKey.size() != primaryKey.size()) {
            throw new IllegalArgumentException("All column metadata for PKs not found: " + primaryKey);
        }
    }

    /**
     * Returns the catalog name of the table.
     *
     * @return the catalog name, or {@code null} if not specified
     */
    public @Nullable String catalog() {
        return table.catalog();
    }

    /**
     * Returns the schema name of the table.
     *
     * @return the schema name, or {@code null} if not specified
     */
    public @Nullable String schema() {
        return table.schema();
    }

    /**
     * Returns the name of the table.
     *
     * @return the table name
     */
    public String name() {
        return table.name();
    }

    /**
     * Constructs the fully qualified name of the table by combining the catalog, schema, and table name.
     * The format used is "catalog.schema.name" if a catalog is provided; otherwise, "schema.name".
     *
     * @return a string representing the fully qualified name of the table.
     * If the catalog is null, it returns "schema.name";
     * otherwise, it returns "catalog.schema.name".
     */
    public String qualifiedName() {
        return table.qualifiedName();
    }

    /**
     * Retrieve the primary key expressions' metadata for the table.
     *
     * @return a list of {@code ColumnMetaData} objects representing the metadata
     * of the primary key expressions in the table.
     */
    public List<ColumnMetaData> primaryKey() {
        return primaryKey;
    }

    /**
     * Retrieve a list of metadata for the expressions in the table.
     *
     * @return a list of {@code ColumnMetaData} objects representing the metadata
     * of all expressions in the table.
     */
    public List<ColumnMetaData> columns() {
        return columns;
    }

    /**
     * Retrieve the metadata for a specific column given its name.
     * <p>
     * If the column does not exist, an {@code IllegalArgumentException} is thrown.
     *
     * @param columnName the name of the column whose metadata is to be retrieved
     * @return the {@code ColumnMetaData} object associated with the specified column name
     * @throws IllegalArgumentException if the column is not found or if the provided name is null
     */
    public ColumnMetaData column(final String columnName) {
        return ObjectUtils.requireNonNull(columnMap.get(columnName), () -> new IllegalArgumentException("Column metadata not found: " + columnName));
    }

    /**
     * Check if the specified column name exists in the table.
     *
     * @param columnName the name of the column to check for existence
     * @return true if the column exists, false otherwise
     */
    public boolean hasColumn(final String columnName) {
        return columnMap.containsKey(columnName);
    }

    /**
     * Returns a {@link Table} object representing this table.
     *
     * @return a {@code Table} instance
     */
    public Table table() {
        return table;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TableMetaData) obj;
        return Objects.equals(this.table, that.table) &&
                Objects.equals(this.primaryKey, that.primaryKey) &&
                Objects.equals(this.columns, that.columns);
    }

    @Override
    public int hashCode() {
        return Objects.hash(table, primaryKey, columns);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", TableMetaData.class.getSimpleName() + "[", "]")
                .add("table='" + table + "'")
                .add("primaryKey=" + primaryKey)
                .add("expressions=" + columns)
                .toString();
    }
}
