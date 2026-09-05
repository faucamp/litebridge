package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.StringUtils;
import org.litebridge.db.spi.Table;

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

    /**
     * Retrieves the {@link OrmTable} associated with the specified DTO class.
     *
     * @param dtoClass the DTO class for which the corresponding {@link OrmTable} is to be retrieved;
     *                 must not be null
     * @return the {@link OrmTable} associated with the specified DTO class,
     * or null if no table is mapped to the class
     */
    public @Nullable OrmTable getOrmTable(final Class<?> dtoClass) {
        Objects.requireNonNull(dtoClass, "DTO class cannot be null");
        return dtoTableMap.get(dtoClass);
    }

    /**
     * Retrieves the {@link OrmTable} associated with the specified DTO class, or throws an exception if no table is mapped to the class.
     *
     * @param dtoClass the DTO class for which the corresponding {@link OrmTable} is to be retrieved;
     *                 must not be null
     * @return the {@link OrmTable} associated with the specified DTO class
     * @throws IllegalArgumentException if no table is mapped to the class
     */
    public OrmTable getOrmTableOrThrow(final Class<?> dtoClass) throws IllegalArgumentException {
        return Objects.requireNonNull(getOrmTable(dtoClass), "DTO class not registered: '%s'".formatted(dtoClass.getName()));
    }

    /**
     * Retrieves an {@link OrmTable} associated with the specified DTO class within a given context class.
     * <p>
     * This method first retrieves the {@link OrmTable} for the context class. If the context's table exists,
     * it attempts to retrieve the {@link OrmTable} for the DTO class from the context table's registry. If no
     * context table exists or the registry does not contain the table, the method will return an empty {@link Optional}.
     *
     * @param dtoClass     the DTO class for which the corresponding {@link OrmTable} is to be retrieved; must not be null
     * @param contextClass the class representing the context within which the {@link OrmTable} is being resolved; must not be null
     * @return an {@link Optional} containing the {@link OrmTable} associated with the specified DTO class in the context
     * of the given context class, or an empty {@link Optional} if no such table is found
     */
    public Optional<@Nullable OrmTable> getTableInContext(final Class<?> dtoClass, final Class<?> contextClass) {
        final OrmTable contextOrmTable = getOrmTable(contextClass);

        if (contextOrmTable != null) {
            return Optional.ofNullable(contextOrmTable.getContextTableRegistry().getOrmTable(dtoClass));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Retrieves the {@link OrmTable} associated with the specified DTO class within the context
     * of a given context class.
     * <p>
     * This method first resolves the {@link OrmTable} for the context
     * class and then fetches the {@link OrmTable} for the DTO class from the context table's registry.
     * If no table is mapped to either class, an exception is thrown.
     *
     * @param dtoClass     the DTO class for which the corresponding {@link OrmTable} is to be retrieved;
     *                     must not be null
     * @param contextClass the class representing the context within which the {@link OrmTable}
     *                     is to be resolved; must not be null
     * @return the {@link OrmTable} associated with the specified DTO class in the context of
     * the given context class
     * @throws IllegalArgumentException if no {@link OrmTable} is mapped to the context class or the DTO class
     */
    public OrmTable getTableInContextOrThrow(final Class<?> dtoClass, final Class<?> contextClass) {
        return getOrmTableOrThrow(contextClass)
                .getContextTableRegistry()
                .getOrmTableOrThrow(dtoClass);
    }

    /**
     * Retrieves the {@link OrmTable} associated with the specified table name.
     *
     * @param table the table name in the format "schema.table"
     * @return the {@link OrmTable} associated with the specified table name, or {@code null} if not found
     */
    public @Nullable OrmTable getOrmTable(final String table) {
        final String[] catalogSchemaTable = StringUtils.splitArray(table, '.', 3, true);
        return getOrmTable(catalogSchemaTable[1], catalogSchemaTable[2]);
    }

    public OrmTable getOrmTableOrThrow(final String table) {
        return Objects.requireNonNull(getOrmTable(table), "ORM table not found for: " + table);
    }

    /**
     * Retrieves the {@link OrmTable} associated with the specified schema and table name.
     *
     * @param schema the schema name
     * @param table  the table name
     * @return the {@link OrmTable} associated with the specified schema and table name, or {@code null} if not found
     */
    public @Nullable OrmTable getOrmTable(final String schema, final String table) {
        return schemaTableMap.getOrDefault(schema, Collections.emptyMap())
                .get(table);
    }

    /**
     * Retrieves the {@link OrmTable} associated with the specified table.
     *
     * @param table the table
     * @return the {@link OrmTable} associated with the specified table, or {@code null} if not found
     */
    public @Nullable OrmTable getOrmTable(final Table table) {
        return getOrmTable(StringUtils.blankIfNull(table.schema()), table.name());
    }

    public OrmTable getOrmTableOrThrow(final Table table) {
        return Objects.requireNonNull(getOrmTable(table), "ORM table not found for: " + table);
    }

    /**
     * Checks if a table associated with the specified DTO class exists in the registry.
     *
     * @param dtoClass the DTO class for which the presence of an associated table is to be checked;
     *                 must not be null
     * @return {@code true} if a table is mapped to the specified DTO class, {@code false} otherwise
     */
    public boolean containsOrmTable(final Class<?> dtoClass) {
        return dtoTableMap.containsKey(dtoClass);
    }

    /**
     * Adds an ORM table to the registry.
     *
     * @param dtoClass the DTO class for which the table is to be associated
     * @param ormTable the ORM table to register
     */
    public void addTable(final Class<?> dtoClass, final OrmTable ormTable) {
        dtoTableMap.put(dtoClass, ormTable);
        addTable(ormTable);
        ormTable.getContextTableRegistry().getSchemaTableMap()
                .forEach((schema, tableMap) ->
                        schemaTableMap.computeIfAbsent(schema, k -> new ConcurrentHashMap<>()).putAll(tableMap));
    }

    /**
     * Adds a table to the registry, but do not associate it with a root DTO class.
     * <p>
     * The resulting ORM table will only be able to be queried by name from this registry.
     *
     * @param table the table to register
     */
    public void addTable(final OrmTable table) {
        schemaTableMap.computeIfAbsent(StringUtils.blankIfNull(table.getMetaData().schema()), k -> new ConcurrentHashMap<>())
                .put(table.getMetaData().name(), table);
    }

    /**
     * Returns a stream of all registered tables.
     *
     * @return a stream of all registered tables
     */
    public Stream<OrmTable> tableStream() {
        return schemaTableMap.values().stream()
                .flatMap(tableMap ->
                        tableMap.values().stream());
    }

    /**
     * Returns the SPI table for the specified table name.
     *
     * @param table the table name
     * @return the SPI table
     */
    public Table getOrCreateSpiTable(final String table) {
        final String[] catalogSchemaTable = StringUtils.splitArray(table, '.', 3, true);
        return getOrCreateSpiTable(catalogSchemaTable[0], catalogSchemaTable[1], catalogSchemaTable[2]);
    }

    private Map<String, Map<String, OrmTable>> getSchemaTableMap() {
        return schemaTableMap;
    }

    /**
     * Returns the SPI table for the specified catalog, schema, and table name.
     *
     * @param catalog the catalog name
     * @param schema  the schema name
     * @param table   the table name
     * @return the SPI table
     */
    private Table getOrCreateSpiTable(final String catalog, final String schema, final String table) {
        // If the table has been registered for DTO mapping, use the corresponding Table object, else use the table name directly
        final Table spiTable;
        final OrmTable ormTable = getOrmTable(schema, table);

        if (ormTable != null) {
            spiTable = ormTable.getMetaData().toTable();
        } else {
            spiTable = new Table(catalog, schema, table);
        }

        return spiTable;
    }
}
