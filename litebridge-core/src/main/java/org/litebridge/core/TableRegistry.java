package org.litebridge.core;

import jakarta.annotation.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TableRegistry {

    private Map<Class<?>, Table> tables = new ConcurrentHashMap<>();

    public @Nullable Table getTable(final Class<?> dtoClass) {
        return tables.get(dtoClass);
    }

    public void addTable(final Class<?> dtoClass, final Table table) {
        tables.put(dtoClass, table);
    }
}
