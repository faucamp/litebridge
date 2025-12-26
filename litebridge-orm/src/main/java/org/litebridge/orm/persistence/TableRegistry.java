package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.ObjectUtils;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class TableRegistry {

    private final Map<Class<?>, Table> dtoTableMap = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Table>> schemaTableMap = new ConcurrentHashMap<>();

    public @Nullable Table getTable(final Class<?> dtoClass) {
        ObjectUtils.requireNonNull(dtoClass, "DTO class cannot be null");
        return dtoTableMap.get(dtoClass);
    }

    public Table getTableOrThrow(final Class<?> dtoClass) throws IllegalArgumentException {
        return ObjectUtils.requireNonNull(getTable(dtoClass), "DTO class not registered: '%s'".formatted(dtoClass.getName()));
    }

    public @Nullable Table getTable(final String schema, final String table) {
        return schemaTableMap.getOrDefault(schema, Collections.emptyMap())
                .get(table);
    }

    public boolean containsTable(final Class<?> dtoClass) {
        return dtoTableMap.containsKey(dtoClass);
    }

    public void addTable(final Class<?> dtoClass, final Table table) {
        dtoTableMap.put(dtoClass, table);
        schemaTableMap.computeIfAbsent(table.getMetaData().schema(), k -> new ConcurrentHashMap<>())
                .put(table.getMetaData().name(), table);
    }

    public org.litebridge.db.spi.Table getOrCreateSpiTable(final String schema, final String table) {
        // If the table has been registered for DTO mapping, use the corresponding Table object, else use the table name directly
        final org.litebridge.db.spi.Table spiTable;
        final Table tableImpl = getTable(schema, table);

        if (tableImpl != null && Objects.equals(schema, tableImpl.getMetaData().schema())) {
            spiTable = tableImpl.getMetaData();
        } else {
            spiTable = new org.litebridge.db.spi.Table("", schema, table);
        }

        return spiTable;
    }
}
