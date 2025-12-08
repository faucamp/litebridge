package org.litebridge.db.api;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class TableMetaData {

    private final String catalog;
    private final String schema;
    private final String table;
    private final List<String> primaryKey;
    private final Map<String, Column> columns;

    public TableMetaData(String catalog, String schema, String table, final List<String> primaryKey, List<Column> columns) {
        this.catalog = catalog;
        this.schema = schema;
        this.table = table;
        this.primaryKey = primaryKey;
        this.columns = columns.stream()
                .collect(Collectors.toMap(Column::name,
                        Function.identity(),
                        (oldValue, newValue) -> newValue,
                        LinkedHashMap::new));
    }

    public String catalog() {
        return catalog;
    }

    public String schema() {
        return schema;
    }

    public String table() {
        return table;
    }

    public List<String> primaryKey() {
        return primaryKey;
    }

    public Map<String, Column> columns() {
        return columns;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TableMetaData) obj;
        return Objects.equals(this.catalog, that.catalog) &&
                Objects.equals(this.schema, that.schema) &&
                Objects.equals(this.table, that.table) &&
                Objects.equals(this.primaryKey, that.primaryKey) &&
                Objects.equals(this.columns, that.columns);
    }

    @Override
    public int hashCode() {
        return Objects.hash(catalog, schema, table, primaryKey, columns);
    }

    @Override
    public String toString() {
        return "TableMetaData[" +
                "catalog=" + catalog + ", " +
                "schema=" + schema + ", " +
                "table=" + table + ", " +
                "primaryKey=" + primaryKey + ", " +
                "columns=" + columns + ']';
    }


}
