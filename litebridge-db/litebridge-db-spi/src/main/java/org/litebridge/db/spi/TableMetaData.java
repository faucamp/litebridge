package org.litebridge.db.spi;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class TableMetaData {

    private final String catalog;
    private final String schema;
    private final String table;
    private final List<String> primaryKey;
    private final LinkedHashMap<String, Column> columns;

    public TableMetaData(String catalog, String schema, String table, final List<String> primaryKey, List<Column> columns) {
        this.catalog = catalog;
        this.schema = schema;
        this.table = table;
        this.primaryKey = primaryKey;
        this.columns = columns.stream()
                .collect(Collectors.toMap(Column::getName,
                        Function.identity(),
                        (oldValue, newValue) -> newValue,
                        LinkedHashMap::new));
    }

    public String getCatalog() {
        return catalog;
    }

    public String getSchema() {
        return schema;
    }

    public String getTable() {
        return table;
    }

    public List<String> getPrimaryKey() {
        return primaryKey;
    }

    public LinkedHashMap<String, Column> getColumns() {
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
