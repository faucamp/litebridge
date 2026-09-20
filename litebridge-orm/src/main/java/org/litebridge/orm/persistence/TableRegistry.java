package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Table;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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

    private final Map<Class<?>, OrmTable> dtoOrmTableMap = new ConcurrentHashMap<>();
    private final Map<String, OrmTable> ormTableMap = new ConcurrentHashMap<>();
    private final Map<String, Table> spiTableMap = new ConcurrentHashMap<>();

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
        return dtoOrmTableMap.get(dtoClass);
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
    public @Nullable OrmTable getOrmTableInContext(final Class<?> dtoClass, final Class<?> contextClass) {
        final OrmTable contextOrmTable = getOrmTable(contextClass);

        if (contextOrmTable != null) {
            return contextOrmTable.getContextTableRegistry().getOrmTable(dtoClass);
        }

        return null;
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
    public OrmTable getOrmTableInContextOrThrow(final Class<?> dtoClass, final Class<?> contextClass) {
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
        return ormTableMap.get(table);
    }

    /**
     * Retrieves the {@link OrmTable} associated with the specified table name, throwing an exception if not found.
     *
     * @param table the table name, optionally schema-qualified
     * @return the associated {@link OrmTable}
     * @throws NullPointerException if the table is not found
     */
    public OrmTable getOrmTableOrThrow(final String table) {
        return Objects.requireNonNull(getOrmTable(table), "ORM table not found for: " + table);
    }

    /**
     * Retrieves the {@link OrmTable} associated with the specified table.
     *
     * @param table the table
     * @return the {@link OrmTable} associated with the specified table, or {@code null} if not found
     */
    public @Nullable OrmTable getOrmTable(final Table table) {
        return getOrmTable(table.qualifiedName());
    }

    /**
     * Retrieves the {@link OrmTable} associated with the specified SPI table, throwing an exception if not found.
     *
     * @param table the SPI table
     * @return the associated {@link OrmTable}
     * @throws NullPointerException if the table is not found
     */
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
        return dtoOrmTableMap.containsKey(dtoClass);
    }

    /**
     * Adds an ORM table to the registry.
     *
     * @param dtoClass the DTO class for which the table is to be associated
     * @param ormTable the ORM table to register
     */
    public void addTable(final Class<?> dtoClass, final OrmTable ormTable) {
        dtoOrmTableMap.put(dtoClass, ormTable);
        addTable(ormTable);
        ormTable.getContextTableRegistry().getOrmTableMap()
                .forEach((tableName, contextTable) ->
                        ormTableMap.computeIfAbsent(tableName, k -> contextTable));
    }

    /**
     * Adds a table to the registry, but do not associate it with a root DTO class.
     * <p>
     * The resulting ORM table will only be able to be queried by name from this registry.
     *
     * @param ormTable the table to register
     */
    public void addTable(final OrmTable ormTable) {
        ormTableMap.put(ormTable.getMetaData().table().qualifiedName(), ormTable);
    }

    /**
     * Returns the SPI table for the specified table name.
     *
     * @param table the table name
     * @return the SPI table
     */
    public Table getOrCreateSpiTable(final String table) {
        return spiTableMap.computeIfAbsent(table, tableName -> {
            // If the table has been registered for DTO mapping, use the corresponding Table object, else use the table name directly
            final Table spiTable;
            final OrmTable ormTable = getOrmTable(tableName);

            if (ormTable != null) {
                spiTable = ormTable.getMetaData().table();
            } else {
                spiTable = new Table(tableName);
            }

            return spiTable;
        });
    }

    private Map<String, OrmTable> getOrmTableMap() {
        return ormTableMap;
    }
}
