package org.litebridge.orm;

import jakarta.annotation.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TableRegistry {

    private Map<Class<?>, Table> tables = new ConcurrentHashMap<>();
    private Map<String, Table> tablesByName = new ConcurrentHashMap<>();

    public @Nullable Table getTable(final Class<?> dtoClass) {
        return tables.get(dtoClass);
    }

    public @Nullable Table getTable(final String tableName) {
        return tablesByName.get(tableName);
    }

    public boolean containsTable(final Class<?> dtoClass) {
        return tables.containsKey(dtoClass);
    }

    public void addTable(final Class<?> dtoClass, final Table table) {
        tables.put(dtoClass, table);
        tablesByName.put(table.getMetaData().getTable(), table);
    }
}
