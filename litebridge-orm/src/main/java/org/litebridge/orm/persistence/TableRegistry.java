package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.ObjectUtils;
import org.litebridge.db.spi.Table;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The TableRegistry class is a centralized registry responsible for managing the relationship
 * between Data Transfer Object (DTO) classes and their corresponding ORM table representations
 * ({@link OrmTable}). This registry allows for efficient management, querying, and consistency of ORM
 * table mappings within Litebridge.
 * <p>
 * This class provides methods to:
 * - Add and register {@link OrmTable} instances associated with DTO classes.
 * - Retrieve {@link OrmTable} instances based on DTO class, schema, or table names.
 * - Check for the existence of tables mapped to specific DTO classes.
 * - Retrieve or create lightweight SPI table representations for database interactions.
 * <p>
 * Instances of this class are immutable with respect to their internal maps, ensuring thread safety
 * and consistent state across multiple threads.
 */
public final class TableRegistry {

    private final Map<Class<?>, OrmTable> dtoTableMap = new ConcurrentHashMap<>();
    private final Map<String, Map<String, OrmTable>> schemaTableMap = new ConcurrentHashMap<>();

    public @Nullable OrmTable getTable(final Class<?> dtoClass) {
        ObjectUtils.requireNonNull(dtoClass, "DTO class cannot be null");
        return dtoTableMap.get(dtoClass);
    }

    public OrmTable getTableOrThrow(final Class<?> dtoClass) throws IllegalArgumentException {
        return ObjectUtils.requireNonNull(getTable(dtoClass), "DTO class not registered: '%s'".formatted(dtoClass.getName()));
    }

    public OrmTable getTableInContextOrThrow(final Class<?> dtoClass, final Class<?> contextClass) {
        return getTableOrThrow(contextClass)
                .getContextTableRegistry()
                .getTableOrThrow(dtoClass);
    }

    public @Nullable OrmTable getTable(final String schema, final String table) {
        return schemaTableMap.getOrDefault(schema, Collections.emptyMap())
                .get(table);
    }

    public @Nullable OrmTable getTable(final Table table) {
        return getTable(table.schema(), table.name());
    }

    public boolean containsTable(final Class<?> dtoClass) {
        return dtoTableMap.containsKey(dtoClass);
    }

    public void addTable(final Class<?> dtoClass, final OrmTable table) {
        dtoTableMap.put(dtoClass, table);
        schemaTableMap.computeIfAbsent(table.getMetaData().schema(), k -> new ConcurrentHashMap<>())
                .put(table.getMetaData().name(), table);
    }

    public org.litebridge.db.spi.Table getOrCreateSpiTable(final String schema, final String table) {
        // If the table has been registered for DTO mapping, use the corresponding Table object, else use the table name directly
        final org.litebridge.db.spi.Table spiTable;
        final OrmTable tableImpl = getTable(schema, table);

        if (tableImpl != null && Objects.equals(schema, tableImpl.getMetaData().schema())) {
            spiTable = tableImpl.getMetaData();
        } else {
            spiTable = new org.litebridge.db.spi.Table("", schema, table);
        }

        return spiTable;
    }
}
