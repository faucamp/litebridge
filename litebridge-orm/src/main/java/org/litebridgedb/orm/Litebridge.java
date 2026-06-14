package org.litebridgedb.orm;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.commons.CollectionUtils;
import org.litebridgedb.db.spi.Aliased;
import org.litebridgedb.db.spi.DatabaseProvider;
import org.litebridgedb.db.spi.Row;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.tx.TransactionManager;
import org.litebridgedb.orm.api.delete.DeleteQuery;
import org.litebridgedb.orm.api.delete.DeleteTerminal;
import org.litebridgedb.orm.api.dto.DtoFromClauseTerminal;
import org.litebridgedb.orm.api.dto.DtoSelectSpec;
import org.litebridgedb.orm.api.dto.DtoSelector;
import org.litebridgedb.orm.api.dto.delete.DtoDeleteWhereClause;
import org.litebridgedb.orm.api.dto.delete.DtoDeletor;
import org.litebridgedb.orm.api.dto.update.DtoUpdateStart;
import org.litebridgedb.orm.api.dto.update.DtoUpdater;
import org.litebridgedb.orm.api.register.DtoTableSpecBuilder;
import org.litebridgedb.orm.api.register.RegistrationContext;
import org.litebridgedb.orm.api.register.RegistrationContextTerminal;
import org.litebridgedb.orm.api.register.TypeSafeDtoTableMapping;
import org.litebridgedb.orm.api.spec.DtoTableSpec;
import org.litebridgedb.orm.api.sql.SqlFromClause;
import org.litebridgedb.orm.api.sql.SqlSelector;
import org.litebridgedb.orm.api.sql.delete.SqlDeleteWhereClause;
import org.litebridgedb.orm.api.sql.delete.SqlDeletor;
import org.litebridgedb.orm.api.sql.update.SqlUpdateStart;
import org.litebridgedb.orm.api.sql.update.SqlUpdater;
import org.litebridgedb.orm.api.tx.TransactionContext;
import org.litebridgedb.orm.api.update.UpdateQuery;
import org.litebridgedb.orm.api.update.UpdateTerminal;
import org.litebridgedb.orm.config.LitebridgeConfig;
import org.litebridgedb.orm.config.RelatedDtoStrategy;
import org.litebridgedb.orm.persistence.DtoConstructor;
import org.litebridgedb.orm.persistence.DtoEntityMapping;
import org.litebridgedb.orm.persistence.EntityDtoMapper;
import org.litebridgedb.orm.persistence.OrmTable;
import org.litebridgedb.orm.persistence.PersistenceFacade;
import org.litebridgedb.orm.persistence.SelectSpecDtoMapper;
import org.litebridgedb.orm.persistence.TableMapper;
import org.litebridgedb.orm.persistence.TableRegistry;
import org.litebridgedb.orm.persistence.TransactionalDatabaseProvider;
import org.litebridgedb.orm.persistence.alias.AliasGenerator;
import org.litebridgedb.orm.persistence.alias.DefaultAliasGenerator;
import org.litebridgedb.orm.persistence.alias.NoOpAliasGenerator;
import org.litebridgedb.orm.persistence.register.AnnotationMapper;
import org.litebridgedb.orm.tx.DefaultTransactionManager;
import org.litebridgedb.tracking.ChangeTracker;
import org.litebridgedb.tracking.FieldAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.lang.invoke.MethodHandles;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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
public final class Litebridge {

    private static final Logger LOGGER = LoggerFactory.getLogger(Litebridge.class);
    private static final Aliased[] ALL_COLUMNS = new Aliased[0];

    private final TableRegistry tableRegistry = new TableRegistry();
    private final TransactionalDatabaseProvider databaseProvider;
    private final TransactionContext transactionContext;
    private final PersistenceFacade persistenceFacade;
    private final TableMapper tableMapper;
    private final ChangeTracker changeTracker;
    private final MethodHandles.Lookup lookup;
    private final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);
    private final LitebridgeConfig litebridgeConfig;
    private @Nullable List<FieldAccessor> pendingManyToOneDependencies;

    /**
     * Constructs a Litebridge instance with the specified database provider and data source.
     * <p>
     * It uses the default method handle lookup, and Litebridge's {@link DefaultTransactionManager} to manage transactions.
     *
     * @param databaseProvider the database provider to be used by Litebridge.
     *                         This parameter is required to set up database operations
     *                         and facilitate persistence functionalities. Must not be null.
     * @param dataSource       the data source to be used by Litebridge.
     *                         This parameter is required to provide database connectivity
     *                         and facilitate persistence operations. Must not be null.
     */
    public Litebridge(final DatabaseProvider databaseProvider,
                      final DataSource dataSource) {
        this(databaseProvider, dataSource, null, MethodHandles.lookup());
    }

    /**
     * Constructs a Litebridge instance with the specified database provider and data source.
     * <p>
     * It uses the default method handle lookup, and Litebridge's {@link DefaultTransactionManager} to manage transactions.
     *
     * @param databaseProvider the database provider to be used by Litebridge.
     *                         This parameter is required to set up database operations
     *                         and facilitate persistence functionalities. Must not be null.
     * @param dataSource       the data source to be used by Litebridge.
     *                         This parameter is required to provide database connectivity
     *                         and facilitate persistence operations. Must not be null.
     * @param litebridgeConfig global runtime configuration
     */
    public Litebridge(final DatabaseProvider databaseProvider,
                      final DataSource dataSource,
                      final @Nullable LitebridgeConfig litebridgeConfig) {
        this(databaseProvider, dataSource, litebridgeConfig, MethodHandles.lookup());
    }

    /**
     * Constructs a Litebridge instance with the specified database provider and data source.
     * <p>
     * It uses Litebridge's {@link DefaultTransactionManager} to manage transactions.
     *
     * @param databaseProvider the database provider to be used by Litebridge.
     *                         This parameter is required to set up database operations
     *                         and facilitate persistence functionalities. Must not be null.
     * @param dataSource       the data source to be used by Litebridge.
     *                         This parameter is required to provide database connectivity
     *                         and facilitate persistence operations. Must not be null.
     * @param litebridgeConfig global runtime configuration
     * @param lookup           the MethodHandles.Lookup instance used for method and field lookups during change tracking
     */
    public Litebridge(final DatabaseProvider databaseProvider,
                      final DataSource dataSource,
                      final @Nullable LitebridgeConfig litebridgeConfig,
                      final MethodHandles.Lookup lookup) {
        this(databaseProvider, new DefaultTransactionManager(dataSource), litebridgeConfig, lookup);
    }

    /**
     * Constructs a Litebridge instance with the specified database provider,
     * transaction manager, and default method handle lookup.
     *
     * @param databaseProvider   the provider responsible for managing database connections
     * @param transactionManager the manager responsible for handling database transactions
     */
    public Litebridge(final DatabaseProvider databaseProvider,
                      final TransactionManager transactionManager) {
        this(databaseProvider, transactionManager, new LitebridgeConfig());
    }

    /**
     * Constructs a Litebridge instance with the specified database provider,
     * transaction manager, and default method handle lookup.
     *
     * @param databaseProvider   the provider responsible for managing database connections
     * @param transactionManager the manager responsible for handling database transactions
     * @param litebridgeConfig   global runtime configuration
     */
    public Litebridge(final DatabaseProvider databaseProvider,
                      final TransactionManager transactionManager,
                      final LitebridgeConfig litebridgeConfig) {
        this(databaseProvider, transactionManager, litebridgeConfig, MethodHandles.lookup());
    }

    /**
     * Constructs an instance of Litebridge, responsible for managing database operations,
     * transaction contexts, change tracking, and persistence functionality.
     *
     * @param databaseProvider   the provider responsible for supplying database connections and operations
     * @param transactionManager the manager that handles transaction lifecycles and operations
     * @param lookup             the MethodHandles.Lookup instance used for method and field lookups during change tracking
     */
    public Litebridge(final DatabaseProvider databaseProvider,
                      final TransactionManager transactionManager,
                      final MethodHandles.Lookup lookup) {
        this(databaseProvider, transactionManager, null, lookup);
    }

    /**
     * Constructs an instance of Litebridge, responsible for managing database operations,
     * transaction contexts, change tracking, and persistence functionality.
     *
     * @param databaseProvider   the provider responsible for supplying database connections and operations
     * @param transactionManager the manager that handles transaction lifecycles and operations
     * @param litebridgeConfig   global runtime configuration
     * @param lookup             the MethodHandles.Lookup instance used for method and field lookups during change tracking
     */
    public Litebridge(final DatabaseProvider databaseProvider,
                      final TransactionManager transactionManager,
                      final @Nullable LitebridgeConfig litebridgeConfig,
                      final MethodHandles.Lookup lookup) {
        this.databaseProvider = new TransactionalDatabaseProvider(transactionManager, databaseProvider);
        this.transactionContext = new TransactionContext(transactionManager);
        this.litebridgeConfig = litebridgeConfig != null ? litebridgeConfig : new LitebridgeConfig();
        this.changeTracker = new ChangeTracker(lookup);
        this.persistenceFacade = new PersistenceFacade(tableRegistry, this.databaseProvider, changeTracker, dtoConstructor);
        this.lookup = lookup;
        this.tableMapper = new TableMapper(this.databaseProvider, tableRegistry, changeTracker);
    }

    /**
     * Registers a DTO class along with its associated table specification using the provided registration context function.
     *
     * @param dtoClass The class object of the DTO (Data Transfer Object) to be registered.
     * @param rc       A function that takes a RegistrationContext instance to configure the table mapping.
     */
    public void register(final Class<?> dtoClass, final Function<RegistrationContext, RegistrationContextTerminal> rc) {
        final RegistrationContextTerminal context = rc.apply(new RegistrationContext(dtoClass, databaseProvider));
        register(new DtoTableSpecBuilder(context).build());
    }

    /**
     * Registers DTO table specification(s) using the provided type-safe DTO table mapping(s).
     *
     * @param typeSafeDtoTableMappings one or more type-safe DTO table mappings to create and register DTO table specifications for
     */
    public void register(final TypeSafeDtoTableMapping... typeSafeDtoTableMappings) {
        final DtoTableSpec[] dtoTableSpecs = Arrays.stream(typeSafeDtoTableMappings)
                .map(typeSafeDtoTableMapping -> typeSafeDtoTableMapping.createDtoTableSpec(databaseProvider))
                .toArray(DtoTableSpec[]::new);

        register(dtoTableSpecs);
    }

    /**
     * Registers annotated entity class(es).
     * <p>
     * The annotated entity must be annotated with {@link Table} and contain at least one field annotated with
     * {@link org.litebridgedb.orm.annotation.Column}, {@link org.litebridgedb.orm.annotation.OneToMany} or {@link org.litebridgedb.orm.annotation.ManyToMany}.
     *
     * @param entityClasses the class(es) of the entity/entities to be registered.
     */
    public void register(final Class<?>... entityClasses) {
        final DtoTableSpec[] dtoTableSpecs = new DtoTableSpec[entityClasses.length];

        for (int i = 0; i < entityClasses.length; i++) {
            LOGGER.debug("Registering entity class '{}'", entityClasses[i]);
            dtoTableSpecs[i] = AnnotationMapper.createDtoTableSpec(entityClasses[i], databaseProvider, lookup);
        }

        register(dtoTableSpecs);
    }

    /**
     * Register a Data Transfer Object (DTO) class(es) with its corresponding table specification(s).
     * <p>
     * This method maps the DTO class to a database table and stores the association
     * in the table registry to enable database operations such as insert, update, or query.
     *
     * @param dtoTableSpecs One or more DTO-to-table mapping details
     */
    public void register(final DtoTableSpec... dtoTableSpecs) {
        final Set<Class<?>> allDtoClasses = new HashSet<>(dtoTableSpecs.length);

        for (final DtoTableSpec dtoTableSpec : dtoTableSpecs) {
            allDtoClasses.add(dtoTableSpec.dtoClass());
            allDtoClasses.addAll(dtoTableSpec.dtoInterfaces());
        }

        for (final DtoTableSpec dtoTableSpec : dtoTableSpecs) {
            final Class<?> dtoClass = dtoTableSpec.dtoClass();
            try {
                final MethodHandles.Lookup elevatedLookup = MethodHandles.privateLookupIn(dtoClass, lookup);
                changeTracker.classFieldAccessorCache().registerElevatedLookup(dtoClass, elevatedLookup);
            } catch (IllegalAccessException e) {
                // If we can't create a private lookup, the tracking will fall back to the provided lookup
                // which might fail if the module is not open to litebridge-tracking.
                // This is expected if the user hasn't opened their module to litebridge.orm either.
                LOGGER.warn("Failed to create elevated lookup for DTO class '{}'. Ensure the module is open to litebridge.orm.", dtoClass.getName());
            }

            LOGGER.trace("Registering DtoTableSpec for DTO class '{}'", dtoClass);
            final TableMapper.MappedTable mappedTable = tableMapper.mapToTable(lookup, dtoClass, dtoTableSpec.tableSpec(), allDtoClasses);
            final OrmTable ormTable = mappedTable.ormTable();
            tableRegistry.addTable(dtoTableSpec.dtoClass(), ormTable);

            if (!CollectionUtils.isEmpty(dtoTableSpec.dtoInterfaces())) {
                ormTable.setDtoClassInterfaces(new HashSet<>(dtoTableSpec.dtoInterfaces()));
                dtoTableSpec.dtoInterfaces().forEach(dtoInterface -> tableRegistry.addTable(dtoInterface, ormTable));
            }

            if (!ormTable.getNestedDtoClasses().isEmpty()) {
                ormTable.getNestedDtoClasses().forEach(nestedDtoClass -> tableRegistry.addTable(nestedDtoClass, ormTable));
            }

            // Process pending many-to-one dependencies for this class
            if (!CollectionUtils.isEmpty(pendingManyToOneDependencies)) {
                final Iterator<FieldAccessor> iterator = pendingManyToOneDependencies.iterator();

                while (iterator.hasNext()) {
                    final FieldAccessor fieldAccessor = iterator.next();

                    if (fieldAccessor.genericType() == dtoTableSpec.dtoClass()) {
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

    /**
     * Updates the data in the database for the given DTO class by applying the specified update query.
     *
     * @param <DTO>    The type of the Data Transfer Object (DTO) to be updated.
     * @param dtoClass The class of the DTO that determines the table to be updated.
     * @param update   A function that builds the update query using the provided {@link DtoUpdateStart}.
     */
    public <DTO> void update(final Class<DTO> dtoClass, final Function<DtoUpdateStart<DTO>, UpdateQuery> update) {
        final OrmTable ormTable = tableRegistry.getTableOrThrow(dtoClass);
        final DtoUpdater<DTO> dtoUpdater = new DtoUpdater<>(dtoClass, ormTable, tableRegistry, changeTracker.classFieldAccessorCache(), databaseProvider);
        final UpdateTerminal updateTerminal = (UpdateTerminal) update.apply(dtoUpdater);
        updateTerminal.execute();
    }

    /**
     * Executes an update operation on the specified table using the provided query function.
     *
     * @param tableName the name of the table to update
     * @param query     a function that defines the update query, transforming a {@code SqlUpdateStart}
     *                  instance into an {@code UpdateQuery}
     */
    public void update(final String tableName, final Function<SqlUpdateStart, UpdateQuery> query) {
        final Table table = tableRegistry.getOrCreateSpiTable(tableName);
        final SqlUpdater sqlUpdater = new SqlUpdater(table, databaseProvider);
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
        return select(dtoClass, (RelatedDtoStrategy) null);
    }

    /**
     * Select a registered Data Transfer Object (DTO) type for database query operations.
     *
     * @param <DTO>    The type of the DTO to select.
     * @param dtoClass The class of the DTO to be queried, which must already be registered.
     * @return A {@link DtoSelector} instance for querying and retrieving data for the specified DTO class.
     * @throws IllegalArgumentException if the specified DTO class is not registered in the table registry.
     */
    public <DTO> DtoFromClauseTerminal<DTO> select(final Class<DTO> dtoClass, final @Nullable RelatedDtoStrategy relatedDtoStrategy) {
        final AliasGenerator aliasGenerator = new DefaultAliasGenerator(databaseProvider.getAliasTransformer());
        final OrmTable table = tableRegistry.getTableOrThrow(dtoClass);

        final LitebridgeConfig activeConfig;

        if (relatedDtoStrategy != null) {
            activeConfig = new LitebridgeConfig(litebridgeConfig);
            activeConfig.setRelatedDtoStrategy(relatedDtoStrategy);
        } else {
            activeConfig = litebridgeConfig;
        }

        return new DtoSelector<>(dtoClass, table, tableRegistry, changeTracker.classFieldAccessorCache(), dtoConstructor, databaseProvider, aliasGenerator, activeConfig).select();
    }

    public <DTO> DtoFromClauseTerminal<DTO> select(final Class<DTO> dtoClass, final Class<?> contextDtoClass) {
        final OrmTable table = tableRegistry.getTableInContextOrThrow(dtoClass, contextDtoClass);
        final AliasGenerator aliasGenerator = new DefaultAliasGenerator(databaseProvider.getAliasTransformer());
        return new DtoSelector<>(dtoClass, table, tableRegistry, changeTracker.classFieldAccessorCache(), dtoConstructor, databaseProvider, aliasGenerator, litebridgeConfig).select();
    }

    /**
     * Query data from the database, without mapping results to Data Transfer Objects (DTOs).
     * <p>
     * Creates a SQL SELECT statement with the specified expressions; the source table is specified
     * via a chained {@code from()} call.
     * <p>
     * This method constructs a {@link SqlFromClause} for further query composition
     * by specifying the expressions to be included in the SELECT clause.
     *
     * @param columns An array of column names to be included in the SELECT statement.
     *                Each column name must be a valid, non-null string.
     * @return A {@link SqlFromClause} instance allowing further refinement of
     * the SQL query, such as specifying the table or additional clauses.
     */
    public SqlFromClause select(final String... columns) {
        return new SqlSelector(databaseProvider, tableRegistry, litebridgeConfig).select(columns);
    }

    /**
     * Query data from the database, without mapping results to Data Transfer Objects (DTOs).
     * <p>
     * Creates a SQL SELECT statement with the specified expressions the source table is specified
     * via a chained {@code from()} call.
     * <p>
     * This method constructs a {@link SqlFromClause} to enable further query composition.
     *
     * @param columns An array of {@link Aliased} objects representing the expressions
     *                to be part of the SELECT statement. Each column must have
     *                a valid name and may optionally include an alias.
     * @return A {@link SqlFromClause} instance that allows further refinement
     * of the query, such as specifying the table or additional clauses.
     */
    public SqlFromClause select(final Aliased... columns) {
        return new SqlSelector(databaseProvider, tableRegistry, litebridgeConfig).select(columns);
    }

    /**
     * Query data from the database, without mapping results to Data Transfer Object (DTOs).
     * <p>
     * Creates a SQL SELECT statement with all expressions. The source table is specified
     * via a chained {@code from()} call.
     * This method constructs a {@link SqlFromClause} to enable further query composition.
     *
     * @return A {@link SqlFromClause} instance that allows further refinement
     * of the query, such as specifying the table or additional clauses.
     */
    public SqlFromClause select() {
        return new SqlSelector(databaseProvider, tableRegistry, litebridgeConfig).select(ALL_COLUMNS);
    }

    /**
     * Deletes the specified data transfer object (DTO) using the underlying persistence layer.
     *
     * @param dto the data transfer object to be deleted; must not be null
     * @throws IllegalStateException if the deletion process fails due to a database error
     */
    public void delete(final Object dto) {
        try {
            persistenceFacade.delete(dto);
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to delete DTO: " + dto, ex);
        }
    }

    /**
     * Deletes records from the database for the given DTO class based on the specified query.
     *
     * @param <DTO>    The type of the Data Transfer Object (DTO) representing the table.
     * @param dtoClass The class of the DTO to identify the table for deletion.
     * @param query    A function that builds the delete query using a {@link DtoDeleteWhereClause}.
     */
    public <DTO> void delete(final Class<DTO> dtoClass, final Function<DtoDeleteWhereClause<DTO>, DeleteQuery> query) {
        final OrmTable ormTable = tableRegistry.getTableOrThrow(dtoClass);
        final DtoDeletor<DTO> dtoDtoDeletor = new DtoDeletor<>(dtoClass, ormTable, tableRegistry, changeTracker.classFieldAccessorCache(), databaseProvider);
        final DeleteTerminal deleteTerminal = (DeleteTerminal) query.apply(dtoDtoDeletor);
        deleteTerminal.execute();
    }

    /**
     * Deletes the specified DTO type (all records) by providing its class as a parameter.
     *
     * @param <DTO>    The type of the Data Transfer Object to be deleted.
     * @param dtoClass The class type of the DTO that is to be deleted.
     */
    public <DTO> void delete(final Class<DTO> dtoClass) {
        delete(dtoClass, dtoDeletor -> dtoDeletor);
    }

    /**
     * Deletes records from the specified table in the database based on the provided delete query.
     *
     * @param tableName the name of the table from which records will be deleted
     * @param query     a function that takes an instance of {@code SqlDeleteWhereClause} and returns a {@code DeleteQuery},
     *                  specifying the conditions for deleting the records
     */
    public void delete(final String tableName, final Function<SqlDeleteWhereClause, DeleteQuery> query) {
        final SqlDeletor sqlDeletor = new SqlDeletor(new Table(tableName, null), databaseProvider);
        final DeleteTerminal deleteTerminal = (DeleteTerminal) query.apply(sqlDeletor);
        deleteTerminal.execute();
    }

    /**
     * Deletes all entries from the specified table.
     *
     * @param tableName the name of the table from which to delete entries
     */
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
        final DtoSelectSpec selectSpec = new DtoSelectSpec(dtoClass, ormTable, new NoOpAliasGenerator(), databaseProvider.getSqlFunctionRegistry());
        selectSpec.setFieldColumns(row.columnStream()
                .map(rowColumn -> {
                    final FieldAccessor fieldAccessor = ormTable.getFieldForColumnName(rowColumn.column().name());
                    return new DtoSelectSpec.FieldColumn(fieldAccessor, rowColumn.column());
                })
                .toList());

        final SelectSpecDtoMapper selectSpecDtoMapper = new SelectSpecDtoMapper(selectSpec, databaseProvider.getTypeConverter(), tableRegistry, dtoConstructor, litebridgeConfig);

        final List<DTO> dtos = selectSpecDtoMapper.toDtos(dtoClass, List.of(row));

        if (dtos.isEmpty()) {
            throw new IllegalArgumentException("No DTO could be created from the given row and DTO class.");
        }

        return dtos.getFirst();
    }

    /**
     * Creates an instance of EntityDtoMapper for the specified DTO class and entity-to-DTO mappings.
     * <p>
     * This allows raw row results to be mapped to DTO instances.
     *
     * @param dtoClass          the class of the DTO that the mapper will handle
     * @param dtoEntityMappings the list of mappings between DTO fields and corresponding entity fields
     * @return an instance of EntityDtoMapper configured for the specified DTO class and mappings
     */
    public <DTO> EntityDtoMapper<DTO> entityDtoMapper(final Class<DTO> dtoClass, final List<DtoEntityMapping> dtoEntityMappings) {
        return new EntityDtoMapper<>(dtoClass, dtoEntityMappings, changeTracker.classFieldAccessorCache());
    }

    /**
     * Provides access to the current transaction context.
     * <p>
     * Use this method to start a transaction.
     *
     * @return the current TransactionContext instance associated with this object
     */
    public TransactionContext transaction() {
        return transactionContext;
    }
}
