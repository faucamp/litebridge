package org.litebridge.db.spi;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.ObjectUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class TableMetaData extends Table {

    private final List<ColumnMetaData> primaryKey;
    private final List<ColumnMetaData> columns;
    private final Map<String, ColumnMetaData> columnMap;

    public TableMetaData(final Table table, final List<String> primaryKey, final List<ColumnMetaData> columns) {
        this(table.catalog(), table.schema(), table.name(), primaryKey, columns);
    }

    public TableMetaData(final String catalog, final String schema, final String table, final List<String> primaryKey, final List<ColumnMetaData> columns) {
        super(catalog, schema, table);
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

    private TableMetaData(final TableMetaData other) {
        this(other.catalog(), other.schema(), other.name(), other.primaryKey.stream().map(ColumnMetaData::name).toList(), other.columns);
    }

    public List<ColumnMetaData> primaryKey() {
        return primaryKey;
    }

    public List<ColumnMetaData> columns() {
        return columns;
    }

    public ColumnMetaData column(final String columnName) {
        return ObjectUtils.requireNonNull(columnMap.get(columnName), "Column metadata not found: " + columnName);
    }

    public boolean hasColumn(final String columnName) {
        return columnMap.containsKey(columnName);
    }

    public TableMetaData as(final String alias) {
        final TableMetaData copy = new TableMetaData(this);
        copy.setAlias(alias);
        return copy;
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
        return "TableMetaData[" +
                "catalog=" + catalog() + ", " +
                "schema=" + schema() + ", " +
                "table=" + name() + ", " +
                "primaryKey=" + primaryKey + ", " +
                "columns=" + columns + ']';
    }
}
