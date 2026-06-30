package org.litebridgedb.db.spi;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.commons.ObjectUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
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
public final class TableMetaData {

    /**
     * Database catalog name
     */
    private final @Nullable String catalog;
    /**
     * Database schema name
     */
    private final @Nullable String schema;
    /**
     * Database table name
     */
    private final String name;
    private final List<ColumnMetaData> primaryKey;
    private final List<ColumnMetaData> columns;
    private final Map<String, ColumnMetaData> columnMap;

    /**
     * Construct a {@code TableMetaData} instance using the provided table, primary key, and column metadata.
     *
     * @param table      the {@code Table} object representing the database table; must not be {@code null}
     * @param primaryKey a list of column names representing the primary key of the table; must not be {@code null}
     * @param columns    a list of {@code ColumnMetaData} objects representing the expressions of the table; must not be {@code null}
     * @throws IllegalArgumentException if any primary key column metadata is not found in the provided column metadata
     */
    public TableMetaData(final Table table, final List<String> primaryKey, final List<ColumnMetaData> columns) {
        this(table.catalog(), table.schema(), table.name(), primaryKey, columns);
    }

    /**
     * Construct a {@code TableMetaData} object representing metadata for a database table.
     *
     * @param catalog    the catalog name of the table; may be {@code null} if not applicable
     * @param schema     the schema name of the table; may be {@code null} if not applicable
     * @param table      the name of the table; must not be {@code null}
     * @param primaryKey a list of column names representing the primary key of the table; must not be {@code null}
     * @param columns    a list of {@link ColumnMetaData} objects representing the expressions of the table; must not be {@code null}
     * @throws IllegalArgumentException if any primary key column metadata is not found in the provided column metadata
     */
    public TableMetaData(final @Nullable String catalog, final @Nullable String schema, final String table, final List<String> primaryKey, final List<ColumnMetaData> columns) {
        this.catalog = catalog;
        this.schema = schema;
        this.name = table;
        this.columns = Collections.unmodifiableList(columns);
        this.columnMap = columns.stream()
                .collect(Collectors.toMap(ColumnMetaData::name,
                        Function.identity()));
        this.primaryKey = columns.stream()
                .filter(column -> primaryKey.contains(column.name()))
                .toList();

        if (this.primaryKey.size() != primaryKey.size()) {
            throw new IllegalArgumentException("All column metadata for PKs not found: " + primaryKey);
        }
    }

    public @Nullable String catalog() {
        return catalog;
    }

    public @Nullable String schema() {
        return schema;
    }

    public String name() {
        return name;
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
        return catalog() != null ? catalog() + "." + schema() + "." + name() : schema() + "." + name();
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

    public Table toTable() {
        return new Table(catalog, schema, name);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TableMetaData) obj;
        return Objects.equals(this.catalog(), that.catalog()) &&
                Objects.equals(this.schema(), that.schema()) &&
                Objects.equals(this.name(), that.name()) &&
                Objects.equals(this.primaryKey, that.primaryKey) &&
                Objects.equals(this.columns, that.columns);
    }

    @Override
    public int hashCode() {
        return Objects.hash(catalog(), schema(), name(), primaryKey, columns);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", TableMetaData.class.getSimpleName() + "[", "]")
                .add("catalog='" + catalog + "'")
                .add("schema='" + schema + "'")
                .add("name='" + name + "'")
                .add("primaryKey=" + primaryKey)
                .add("expressions=" + columns)
                .toString();
    }
}
