package org.litebridgedb.orm.persistence;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.commons.StringUtils;
import org.litebridgedb.db.spi.Table;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

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
        Objects.requireNonNull(dtoClass, "DTO class cannot be null");
        return dtoTableMap.get(dtoClass);
    }

    public OrmTable getTableOrThrow(final Class<?> dtoClass) throws IllegalArgumentException {
        return Objects.requireNonNull(getTable(dtoClass), "DTO class not registered: '%s'".formatted(dtoClass.getName()));
    }

    public Optional<@Nullable OrmTable> getTableInContext(final Class<?> dtoClass, final Class<?> contextClass) {
        final OrmTable contextOrmTable = getTable(contextClass);

        if (contextOrmTable != null) {
            return Optional.ofNullable(contextOrmTable.getContextTableRegistry().getTable(dtoClass));
        } else {
            return Optional.empty();
        }
    }

    public OrmTable getTableInContextOrThrow(final Class<?> dtoClass, final Class<?> contextClass) {
        return getTableOrThrow(contextClass)
                .getContextTableRegistry()
                .getTableOrThrow(dtoClass);
    }

    public @Nullable OrmTable getTable(final String table) {
        final String[] catalogSchemaTable = StringUtils.splitArray(table, '.', 3, true);
        return getTable(catalogSchemaTable[1], catalogSchemaTable[2]);
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
        schemaTableMap.computeIfAbsent(StringUtils.blankIfNull(table.getMetaData().schema()), k -> new ConcurrentHashMap<>())
                .put(table.getMetaData().name(), table);
    }

    public Stream<OrmTable> tableStream() {
        return schemaTableMap.values().stream()
                .flatMap(tableMap ->
                        tableMap.values().stream());
    }

    public Table getOrCreateSpiTable(final String table) {
        final String[] catalogSchemaTable = StringUtils.splitArray(table, '.', 3, true);
        return getOrCreateSpiTable(catalogSchemaTable[0], catalogSchemaTable[1], catalogSchemaTable[2]);
    }

    private Table getOrCreateSpiTable(final String catalog, final String schema, final String table) {
        // If the table has been registered for DTO mapping, use the corresponding Table object, else use the table name directly
        final org.litebridgedb.db.spi.Table spiTable;
        final OrmTable ormTable = getTable(schema, table);

        if (ormTable != null) {
            spiTable = ormTable.getMetaData().toTable();
        } else {
            spiTable = new Table(catalog, schema, table);
        }

        return spiTable;
    }
}
