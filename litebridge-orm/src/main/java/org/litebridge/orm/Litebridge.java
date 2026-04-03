package org.litebridge.orm;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.CollectionUtils;
import org.litebridge.db.spi.Aliased;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.tx.TransactionManager;
import org.litebridge.orm.api.delete.DeleteQuery;
import org.litebridge.orm.api.delete.DeleteTerminal;
import org.litebridge.orm.api.dto.DtoFromClauseTerminal;
import org.litebridge.orm.api.dto.DtoSelectSpec;
import org.litebridge.orm.api.dto.DtoSelector;
import org.litebridge.orm.api.dto.delete.DtoDeleteWhereClause;
import org.litebridge.orm.api.dto.delete.DtoDeletor;
import org.litebridge.orm.api.dto.update.DtoUpdateStart;
import org.litebridge.orm.api.dto.update.DtoUpdater;
import org.litebridge.orm.api.spec.TableMapping;
import org.litebridge.orm.api.spec.TableSpec;
import org.litebridge.orm.api.sql.SqlFromClause;
import org.litebridge.orm.api.sql.SqlSelector;
import org.litebridge.orm.api.sql.delete.SqlDeleteWhereClause;
import org.litebridge.orm.api.sql.delete.SqlDeletor;
import org.litebridge.orm.api.sql.update.SqlUpdateStart;
import org.litebridge.orm.api.sql.update.SqlUpdater;
import org.litebridge.orm.api.tx.TransactionContext;
import org.litebridge.orm.api.update.UpdateQuery;
import org.litebridge.orm.api.update.UpdateTerminal;
import org.litebridge.orm.persistence.AliasGenerator;
import org.litebridge.orm.persistence.DtoEntityMapping;
import org.litebridge.orm.persistence.EntityDtoMapper;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.PersistenceFacade;
import org.litebridge.orm.persistence.SelectSpecDtoMapper;
import org.litebridge.orm.persistence.TableMapper;
import org.litebridge.orm.persistence.TableRegistry;
import org.litebridge.orm.persistence.TransactionalDatabaseProvider;
import org.litebridge.orm.tx.DefaultTransactionManager;
import org.litebridge.tracking.ChangeTracker;
import org.litebridge.tracking.FieldAccessor;

import javax.sql.DataSource;
import java.lang.invoke.MethodHandles;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Primary entry point for Litebridge.
 * <p>
 * Litebridge is responsible for managing database interactions,
 * including mapping Data Transfer Objects (DTOs) to tables,
 * registering tables, change tracking, and executing query operations.
 * <p>
 * It provides a mechanism to translate between DTOs and database tables,
 * facilitating CRUD operations while maintaining consistency and integrity.
 * <p>
 * Litebridge ensures thread safety by using immutable internal structures and
 * leveraging the {@code DatabaseProvider} and {@code PersistenceFacade} for
 * database interactions, ensuring that operations are performed safely and
 * efficiently.
 */
public class Litebridge {

    private static final Aliased[] ALL_COLUMNS = new Aliased[0];

    private final TableRegistry tableRegistry = new TableRegistry();
    private final TransactionalDatabaseProvider databaseProvider;
    private final TransactionContext transactionContext;
    private final PersistenceFacade persistenceFacade;
    private final TableMapper tableMapper;
    private final ChangeTracker changeTracker;
    private @Nullable List<FieldAccessor> pendingManyToOneDependencies;

    /**
     * Constructs a Litebridge instance with the specified database provider.
     *
     * @param databaseProvider the database provider to be used by Litebridge.
     *                         This parameter is required to set up database operations
     *                         and facilitate persistence functionalities. Must not be null.
     */
    public Litebridge(final DatabaseProvider databaseProvider,
                      final DataSource dataSource) {
        this(databaseProvider, dataSource, new DefaultTransactionManager(dataSource), MethodHandles.lookup());
    }

    public Litebridge(final DatabaseProvider databaseProvider,
                      final DataSource dataSource,
                      final TransactionManager transactionManager) {
        this(databaseProvider, dataSource, transactionManager, MethodHandles.lookup());
    }

    public Litebridge(final DatabaseProvider databaseProvider,
                      final DataSource dataSource,
                      final TransactionManager transactionManager,
                      final MethodHandles.Lookup lookup) {
        this.databaseProvider = new TransactionalDatabaseProvider(transactionManager, databaseProvider);
        this.transactionContext = new TransactionContext(transactionManager);
        this.changeTracker = new ChangeTracker(lookup);
        this.persistenceFacade = new PersistenceFacade(tableRegistry, this.databaseProvider, changeTracker);
        this.tableMapper = new TableMapper(this.databaseProvider, tableRegistry, changeTracker);
    }

    /**
     * Register a Data Transfer Object (DTO) class with its corresponding table specification.
     * This method maps the DTO class to a database table and stores the association
     * in the table registry to enable database operations such as insert, update, or query.
     *
     * @param dtoClass  the class of the Data Transfer Object to be registered; must not be null.
     * @param tableSpec the table specification defining the mapping of the DTO class to the database table; must not be null.
     * @throws SQLException if an error occurs during the mapping or registration process.
     */
    public void register(final MethodHandles.Lookup lookup, final Class<?> dtoClass, final TableSpec tableSpec, final Class<?>... dtoInterfaces) throws SQLException {
        final TableMapper.MappedTable mappedTable = tableMapper.mapToTable(lookup, dtoClass, tableSpec);
        final OrmTable ormTable = mappedTable.ormTable();
        tableRegistry.addTable(dtoClass, mappedTable.ormTable());
        Arrays.stream(dtoInterfaces).forEach(dtoInterface -> tableRegistry.addTable(dtoInterface, ormTable));

        if (!ormTable.getNestedDtoClasses().isEmpty()) {
            ormTable.getNestedDtoClasses().forEach(nestedDtoClass -> tableRegistry.addTable(nestedDtoClass, ormTable));
        }

        // Process pending many-to-one dependencies for this class
        if (!CollectionUtils.isEmpty(pendingManyToOneDependencies)) {
            final Iterator<FieldAccessor> iterator = pendingManyToOneDependencies.iterator();

            while (iterator.hasNext()) {
                final FieldAccessor fieldAccessor = iterator.next();

                if (fieldAccessor.genericType() == dtoClass) {
                    ormTable.addOneToManyReverseMapping(fieldAccessor);
                    iterator.remove();
                }
            }
        }

        // Process/pend this table's dependants)
        mappedTable.manyToOneDependencies().forEach(fieldAccessor -> {
            final OrmTable targetOrmTable = tableRegistry.getTable(fieldAccessor.genericType());

            if (targetOrmTable != null) {
                targetOrmTable.addOneToManyReverseMapping(fieldAccessor);
            } else {
                if (pendingManyToOneDependencies == null) {
                    pendingManyToOneDependencies = new ArrayList<>();
                }

                pendingManyToOneDependencies.add(fieldAccessor);
            }
        });
    }

    public void register(final Class<?> dtoClass, final TableSpec tableSpec, final Class<?>... dtoInterfaces) throws SQLException {
        register(MethodHandles.lookup(), dtoClass, tableSpec, dtoInterfaces);
    }

    public void register(final TableMapping tableMapping) throws SQLException {
        register(tableMapping.lookup(), tableMapping.dtoClass(), tableMapping.tableSpec());
    }

    /**
     * Initiate change tracking for the given Data Transfer Object (DTO).
     * <p>
     * This process involves associating the DTO with its corresponding ORM table
     * and enabling change tracking for the object's fields.
     *
     * @param <T> the type of the Data Transfer Object being tracked.
     * @param dto the Data Transfer Object to be tracked; must not be null and
     *            must be registered with a corresponding table.
     * @return the tracked Data Transfer Object.
     * @throws IllegalArgumentException if the DTO is null or if its class is not registered.
     */
    public <T> T track(final T dto) {
        final OrmTable table = tableRegistry.getTable(Objects.requireNonNull(dto, "DTO cannot be null").getClass());

        if (table == null) {
            throw new IllegalArgumentException("DTO class not registered: '%s'".formatted(dto.getClass().getName()));
        }

        table.trackDto(dto);
        return dto;
    }

    /**
     * Save the given Data Transfer Object (DTO) to the database, via a SQL INSERT or UPDATE statement.
     * <p>
     * This method utilises the persistence facade to perform the save operation. It handles SQL exceptions
     * and ensures the integrity of the save process.
     *
     * @param dtos the Data Transfer Object(s) to be saved in the database.
     * @throws IllegalStateException if an error occurs during the save operation.
     */
    public void save(final Object... dtos) {
        try {
            if (dtos.length == 1) {
                persistenceFacade.save(dtos[0]);
            } else {
                save(List.of(dtos));
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to save DTO: " + dtos[0], ex);
        }
    }

    /**
     * Saves a collection of Data Transfer Objects (DTOs) to the database.
     *
     * @param dtos a collection of objects to save in the database; must not be null.
     * @throws IllegalStateException if an error occurs during the save operation.
     */
    public void save(final Collection<Object> dtos) {
        try {
            persistenceFacade.save(dtos);
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to save DTOs: " + dtos, ex);
        }
    }

    /**
     * Insert the specified Data Transfer Object (DTO) into the database via a SQL INSERT statement.
     * <p>
     * This method uses the persistence facade to perform the insertion
     * and handles any SQL exceptions that might occur during the process.
     *
     * @param dto the Data Transfer Object to be inserted into the database.
     *            It must correspond to a properly configured and valid DTO.
     * @throws IllegalStateException if an error occurs during the insertion process.
     */
    public void insert(final Object dto) {
        try {
            persistenceFacade.insert(dto);
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to insert DTO: " + dto, ex);
        }
    }

    /**
     * Update the specified Data Transfer Object (DTO) in the database via a SQL UPDATE statement.
     * <p>
     * This method utilises the persistence facade to perform the update operation
     * and handles any SQL exceptions that might occur during the process.
     *
     * @param dto the Data Transfer Object to be updated in the database.
     *            It must represent a valid and properly tracked DTO.
     * @throws IllegalStateException if an error occurs while performing the update.
     */
    public void update(final Object dto) {
        try {
            persistenceFacade.update(dto);
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to update DTO: " + dto, ex);
        }
    }

    public <DTO> void update(final Class<DTO> dtoClass, final Function<DtoUpdateStart<DTO>, UpdateQuery> update) {
        final OrmTable ormTable = tableRegistry.getTableOrThrow(dtoClass);
        final DtoUpdater<DTO> dtoUpdater = new DtoUpdater<>(dtoClass, ormTable, tableRegistry, changeTracker.classFieldAccessorCache(), databaseProvider);
        final UpdateTerminal updateTerminal = (UpdateTerminal) update.apply(dtoUpdater);
        updateTerminal.execute();
    }

    public void update(final String tableName, final Function<SqlUpdateStart, UpdateQuery> query) {
        final TableMetaData tableMetaData = Objects.requireNonNull(tableRegistry.getTable(tableName)).getMetaData();
        final SqlUpdater sqlUpdater = new SqlUpdater(tableMetaData, databaseProvider);
        final UpdateTerminal updateTerminal = (UpdateTerminal) query.apply(sqlUpdater);
        updateTerminal.execute();
    }

    /**
     * Select a registered Data Transfer Object (DTO) type for database query operations.
     *
     * @param <DTO>    The type of the DTO to select.
     * @param dtoClass The class of the DTO to be queried, which must already be registered.
     * @return A {@link DtoSelector} instance for querying and retrieving data for the specified DTO class.
     * @throws IllegalArgumentException if the specified DTO class is not registered in the table registry.
     */
    public <DTO> DtoFromClauseTerminal<DTO> select(final Class<DTO> dtoClass) {
        final AliasGenerator aliasGenerator = new AliasGenerator();
        final OrmTable table = tableRegistry.getTableOrThrow(dtoClass);
        return new DtoSelector<>(dtoClass, table, tableRegistry, changeTracker.classFieldAccessorCache(), databaseProvider, aliasGenerator).select();
    }

    public <DTO> DtoFromClauseTerminal<DTO> select(final Class<DTO> dtoClass, final Class<?> contextDtoClass) {
        final OrmTable table = tableRegistry.getTableInContextOrThrow(dtoClass, contextDtoClass);
        final AliasGenerator aliasGenerator = new AliasGenerator();
        return new DtoSelector<>(dtoClass, table, tableRegistry, changeTracker.classFieldAccessorCache(), databaseProvider, aliasGenerator).select();
    }

    /**
     * Query data from the database, without mapping results to Data Transfer Objects (DTOs).
     * <p>
     * Creates a SQL SELECT statement with the specified columns; the source table is specified
     * via a chained {@code from()} call.
     * <p>
     * This method constructs a {@link SqlFromClause} for further query composition
     * by specifying the columns to be included in the SELECT clause.
     *
     * @param columns An array of column names to be included in the SELECT statement.
     *                Each column name must be a valid, non-null string.
     * @return A {@link SqlFromClause} instance allowing further refinement of
     * the SQL query, such as specifying the table or additional clauses.
     */
    public SqlFromClause select(final String... columns) {
        return new SqlSelector(databaseProvider, tableRegistry).select(columns);
    }

    /**
     * Query data from the database, without mapping results to Data Transfer Objects (DTOs).
     * <p>
     * Creates a SQL SELECT statement with the specified columns the source table is specified
     * via a chained {@code from()} call.
     * <p>
     * This method constructs a {@link SqlFromClause} to enable further query composition.
     *
     * @param columns An array of {@link Aliased} objects representing the columns
     *                to be part of the SELECT statement. Each column must have
     *                a valid name and may optionally include an alias.
     * @return A {@link SqlFromClause} instance that allows further refinement
     * of the query, such as specifying the table or additional clauses.
     */
    public SqlFromClause select(final Aliased... columns) {
        return new SqlSelector(databaseProvider, tableRegistry).select(columns);
    }

    /**
     * Query data from the database, without mapping results to Data Transfer Object (DTOs).
     * <p>
     * Creates a SQL SELECT statement with all columns. The source table is specified
     * via a chained {@code from()} call.
     * This method constructs a {@link SqlFromClause} to enable further query composition.
     *
     * @return A {@link SqlFromClause} instance that allows further refinement
     * of the query, such as specifying the table or additional clauses.
     */
    public SqlFromClause select() {
        return new SqlSelector(databaseProvider, tableRegistry).select(ALL_COLUMNS);
    }

    public void delete(final Object dto) {
        try {
            persistenceFacade.delete(dto);
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to delete DTO: " + dto, ex);
        }
    }

    public <DTO> void delete(final Class<DTO> dtoClass, final Function<DtoDeleteWhereClause<DTO>, DeleteQuery> query) {
        final OrmTable ormTable = tableRegistry.getTableOrThrow(dtoClass);
        final DtoDeletor<DTO> dtoDtoDeletor = new DtoDeletor<>(dtoClass, ormTable, tableRegistry, changeTracker.classFieldAccessorCache(), databaseProvider);
        final DeleteTerminal deleteTerminal = (DeleteTerminal) query.apply(dtoDtoDeletor);
        deleteTerminal.execute();
    }

    public <DTO> void delete(final Class<DTO> dtoClass) {
        delete(dtoClass, dtoDeletor -> dtoDeletor);
    }

    public void delete(final String tableName, final Function<SqlDeleteWhereClause, DeleteQuery> query) {
        final SqlDeletor sqlDeletor = new SqlDeletor(new Table(tableName, null), databaseProvider);
        final DeleteTerminal deleteTerminal = (DeleteTerminal) query.apply(sqlDeletor);
        deleteTerminal.execute();
    }

    public void delete(final String tableName) {
        delete(tableName, sqlDeletor -> sqlDeletor);
    }

    /**
     * Convert a given result row into a Data Transfer Object (DTO) of the specified type.
     *
     * @param <DTO>    The type of the Data Transfer Object to be created.
     * @param row      The data row to map, containing column-value pairs. Must not be null.
     * @param dtoClass The class type of the DTO to which the row is to be mapped. Must not be null.
     * @return An instance of the specified DTO type, populated with values from the given row.
     * @throws IllegalArgumentException if the row or dtoClass is null, or if mapping fails due to type mismatches or invalid configurations.
     */
    public <DTO> DTO toDto(final Row row, final Class<DTO> dtoClass) {
        final OrmTable ormTable = tableRegistry.getTableOrThrow(dtoClass);
        final DtoSelectSpec selectSpec = new DtoSelectSpec(dtoClass, ormTable, new AliasGenerator());
        selectSpec.setFieldColumns(row.columnStream()
                .map(rowColumn -> {
                    final FieldAccessor fieldAccessor = ormTable.getFieldForColumnName(rowColumn.column().name());
                    return new DtoSelectSpec.FieldColumn(fieldAccessor, rowColumn.column());
                })
                .toList());

        final SelectSpecDtoMapper selectSpecDtoMapper = new SelectSpecDtoMapper(selectSpec, databaseProvider.getTypeConverter());

        final List<DTO> dtos = selectSpecDtoMapper.toDtos(dtoClass, List.of(row));

        if (dtos.isEmpty()) {
            throw new IllegalArgumentException("No DTO could be created from the given row and DTO class.");
        }

        return dtos.getFirst();
    }

    public <DTO> EntityDtoMapper<DTO> entityDtoMapper(final Class<DTO> dtoClass, final List<DtoEntityMapping> dtoEntityMappings) {
        return new EntityDtoMapper<>(dtoClass, dtoEntityMappings, changeTracker.classFieldAccessorCache());
    }

    public TransactionContext transaction() {
        return transactionContext;
    }
}
