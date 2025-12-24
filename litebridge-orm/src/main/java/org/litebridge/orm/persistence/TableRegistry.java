package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.ObjectUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class TableRegistry {

    private final Map<Class<?>, Table> tables = new ConcurrentHashMap<>();
    private final Map<String, Table> tablesByName = new ConcurrentHashMap<>();

    public @Nullable Table getTable(final Class<?> dtoClass) {
        ObjectUtils.requireNonNull(dtoClass, "DTO class cannot be null");
        return tables.get(dtoClass);
    }

    public Table getTableOrThrow(final Class<?> dtoClass) throws IllegalArgumentException {
        return ObjectUtils.requireNonNull(getTable(dtoClass), "DTO class not registered: '%s'".formatted(dtoClass.getName()));
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
