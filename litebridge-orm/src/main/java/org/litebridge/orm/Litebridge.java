package org.litebridge.orm;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.tx.TransactionManager;
import org.litebridge.db.spi.update.InsertResult;
import org.litebridge.db.spi.update.UpdateResult;
import org.litebridge.orm.api.delete.DeleteQuery;
import org.litebridge.orm.api.delete.DeleteTerminal;
import org.litebridge.orm.api.dto.DtoFromClauseTerminal;
import org.litebridge.orm.api.dto.DtoSelectSpec;
import org.litebridge.orm.api.dto.delete.DtoDeleteWhereClause;
import org.litebridge.orm.api.dto.delete.DtoDeletor;
import org.litebridge.orm.api.insert.DtoInsertIntoStep;
import org.litebridge.orm.api.insert.InsertValuesStep;
import org.litebridge.orm.api.insert.SqlInsertIntoStep;
import org.litebridge.orm.api.merge.DtoMergeUsingStep;
import org.litebridge.orm.api.merge.MergeTerminal;
import org.litebridge.orm.api.merge.SqlMergeUsingStep;
import org.litebridge.orm.api.register.RegistrationContext;
import org.litebridge.orm.api.register.RegistrationContextTerminal;
import org.litebridge.orm.api.select.FromClauseStart;
import org.litebridge.orm.api.select.FromClauseStartTypeOverride;
import org.litebridge.orm.api.select.SelectApi;
import org.litebridge.orm.api.select.ast.SelectNode;
import org.litebridge.orm.api.spec.DtoTableSpec;
import org.litebridge.orm.api.sql.delete.SqlDeleteWhereClause;
import org.litebridge.orm.api.sql.delete.SqlDeletor;
import org.litebridge.orm.api.tx.TransactionContext;
import org.litebridge.orm.api.update.DtoUpdateStart;
import org.litebridge.orm.api.update.SqlUpdateStart;
import org.litebridge.orm.api.update.UpdateQuery;
import org.litebridge.orm.config.LitebridgeConfig;
import org.litebridge.orm.config.RelatedDtoStrategy;
import org.litebridge.orm.engine.FromClauseEngine;
import org.litebridge.orm.engine.InsertEngine;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.MergeEngine;
import org.litebridge.orm.engine.QueryPlanCache;
import org.litebridge.orm.engine.RegistrationEngine;
import org.litebridge.orm.engine.UpdateEngine;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.ProtoColumnExpressionSpec;
import org.litebridge.orm.expression.TypeOverride;
import org.litebridge.orm.expression.TypeOverrideExpressionSpec;
import org.litebridge.orm.expression.intent.ConvertIntent;
import org.litebridge.orm.expression.select.SelectFieldSpec;
import org.litebridge.orm.nativesql.NativeSqlContext;
import org.litebridge.orm.persistence.DtoConstructor;
import org.litebridge.orm.persistence.DtoEntityMapping;
import org.litebridge.orm.persistence.EntityDtoMapper;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.PersistenceFacade;
import org.litebridge.orm.persistence.SelectSpecDtoMapper;
import org.litebridge.orm.persistence.TableMapper;
import org.litebridge.orm.persistence.TableMetaDataCache;
import org.litebridge.orm.persistence.TableRegistry;
import org.litebridge.orm.persistence.TransactionalDatabaseProvider;
import org.litebridge.orm.persistence.alias.AliasGenerator;
import org.litebridge.orm.persistence.alias.DefaultAliasGenerator;
import org.litebridge.orm.persistence.alias.NoOpAliasGenerator;
import org.litebridge.orm.tx.DefaultTransactionManager;
import org.litebridge.tracking.ChangeTracker;
import org.litebridge.tracking.FieldAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.lang.invoke.MethodHandles;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

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
public final class Litebridge implements SelectApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(Litebridge.class);

    private final TableRegistry tableRegistry = new TableRegistry();
    private final TransactionalDatabaseProvider databaseProvider;
    private final TransactionContext transactionContext;
    private final NativeSqlContext nativeSqlContext;
    private final PersistenceFacade persistenceFacade;
    private final ChangeTracker changeTracker;
    private final DtoConstructor dtoConstructor = new DtoConstructor(tableRegistry);
    private final RegistrationEngine registrationEngine;
    private final FromClauseEngine fromClauseEngine;
    private final InsertEngine insertEngine = new InsertEngine(tableRegistry);
    private final UpdateEngine updateEngine = new UpdateEngine(tableRegistry);
    private final QueryPlanCache queryPlanCache = new QueryPlanCache();
    private final LitebridgeConfig litebridgeConfig;
    private final TableMetaDataCache tableMetaDataCache;

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
        this.nativeSqlContext = new NativeSqlContext(this.databaseProvider);
        this.litebridgeConfig = litebridgeConfig != null ? litebridgeConfig : new LitebridgeConfig();
        this.changeTracker = new ChangeTracker(lookup);
        this.tableMetaDataCache = new TableMetaDataCache(this.databaseProvider, transactionManager);
        final TableMapper tableMapper = new TableMapper(this.databaseProvider, tableRegistry, changeTracker, tableMetaDataCache);
        this.registrationEngine = new RegistrationEngine(this.databaseProvider, tableRegistry, tableMapper, changeTracker, lookup);
        //TODO: cleanup context use
        this.fromClauseEngine = new FromClauseEngine(this.databaseProvider, tableRegistry, changeTracker, dtoConstructor, () -> createLitebridgeContext(LitebridgeContext.Mode.DTO));
        this.persistenceFacade = new PersistenceFacade(tableRegistry, this.databaseProvider, changeTracker, dtoConstructor, createLitebridgeContext(LitebridgeContext.Mode.DTO));

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Litebridge initialised with databaseProvider: {}, config: {}", databaseProvider.getClass().getName(), litebridgeConfig);
        }
    }

    /**
     * Registers a DTO class along with its associated table specification using the provided registration context function.
     *
     * @param dtoClass The class object of the DTO (Data Transfer Object) to be registered.
     * @param rc       A function that takes a RegistrationContext instance to configure the table mapping.
     */
    public void register(final Class<?> dtoClass, final Function<RegistrationContext, RegistrationContextTerminal> rc) {
        registrationEngine.register(dtoClass, rc);
    }

    /**
     * Registers annotated entity class(es).
     * <p>
     * The annotated entity must be annotated with {@link Table} and contain at least one field annotated with
     * {@link org.litebridge.orm.annotation.Column}, {@link org.litebridge.orm.annotation.OneToMany} or {@link org.litebridge.orm.annotation.ManyToMany}.
     *
     * @param entityClasses the class(es) of the entity/entities to be registered.
     */
    public void register(final Class<?>... entityClasses) {
        if (entityClasses.length == 0) {
            throw new IllegalArgumentException("No entity classes provided for registration");
        }

        registrationEngine.register(entityClasses);
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
        registrationEngine.register(dtoTableSpecs);
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
        final OrmTable table = tableRegistry.getOrmTable(Objects.requireNonNull(dto, "DTO cannot be null").getClass());

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
     * @param dto the Data Transfer Object(s) to be saved in the database.
     * @throws IllegalStateException if an error occurs during the save operation.
     */
    public void save(final Object dto) {
        try {
            persistenceFacade.save(dto);
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to save DTO: " + dto, ex);
        }
    }

    /**
     * Saves a collection of Data Transfer Objects (DTOs) to the database, via a SQL INSERT or UPDATE statement.
     *
     * @param dtos a collection of objects to save in the database; must not be null.
     * @throws IllegalStateException if an error occurs during the save operation.
     */
    public void saveAll(final Collection<?> dtos) {
        try {
            persistenceFacade.save(dtos);
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to save DTOs: " + dtos, ex);
        }
    }

    /**
     * Saves an array of Data Transfer Objects (DTOs) to the database, via a SQL INSERT or UPDATE statement.
     *
     * @param dtos an array of objects to save in the database; must not be null.
     * @throws IllegalStateException if an error occurs during the save operation.
     */
    public void saveAll(final Object[] dtos) {
        saveAll(Arrays.asList(dtos));
    }

    /**
     * Save the given Data Transfer Objects (DTOs) to the database, via a SQL INSERT or UPDATE statement.
     *
     * @param dto       first Data Transfer Object to be saved in the database.
     * @param otherDtos additional Data Transfer Objects to be saved in the database.
     * @throws IllegalStateException if an error occurs during the save operation.`
     */
    public void saveAll(final Object dto, final Object... otherDtos) {
        saveAll(Stream.concat(Stream.of(dto), Stream.of(otherDtos)).toList());
    }

    /**
     * Inserts the specified Data Transfer Object (DTO) into the database via a SQL INSERT statement.
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

    public InsertResult insert(final Class<?> dtoClass, final Function<DtoInsertIntoStep, InsertValuesStep> insert) {
        return insertEngine.insert(dtoClass, insert, createDtoLitebridgeContext());
    }

    public InsertResult insert(final String tableName, final Function<SqlInsertIntoStep, InsertValuesStep> insert) {
        return insertEngine.insert(tableName, insert, createSqlLitebridgeContext());
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
    public <DTO> UpdateResult update(final Class<DTO> dtoClass, final Function<DtoUpdateStart<DTO>, UpdateQuery> update) {
        return updateEngine.update(dtoClass, update, createDtoLitebridgeContext());
    }

    /**
     * Executes an update operation on the specified table using the provided query function.
     *
     * @param tableName the name of the table to update
     * @param update    a function that defines the update query, transforming a {@code SqlUpdateStart}
     *                  instance into an {@code UpdateQuery}
     */
    public UpdateResult update(final String tableName, final Function<SqlUpdateStart, UpdateQuery> update) {
        return updateEngine.update(tableName, update, createSqlLitebridgeContext());
    }

    @Override
    public <DTO> DtoFromClauseTerminal<DTO> select(final Class<DTO> dtoClass) {
        return select(dtoClass, (RelatedDtoStrategy) null);
    }

    @Override
    public <DTO> DtoFromClauseTerminal<DTO> select(final Class<DTO> dtoClass, final @Nullable RelatedDtoStrategy relatedDtoStrategy) {
        return fromClauseEngine.from(dtoClass, relatedDtoStrategy);
    }

    @Override
    public <DTO> DtoFromClauseTerminal<DTO> select(final Class<DTO> dtoClass, final Class<?> contextDtoClass) {
        return new FromClauseStart(new SelectNode(null, new ExpressionSpec[0], null), fromClauseEngine).from(dtoClass, contextDtoClass);
    }

    @Override
    public FromClauseStart select(final String... fieldsOrColumns) {
        final ExpressionSpec[] expressionSpecs = Arrays.stream(fieldsOrColumns)
                .map(fieldOrColumn -> new ProtoColumnExpressionSpec(SelectFieldSpec.class, fieldOrColumn, null))
                .toArray(ProtoColumnExpressionSpec[]::new);
        return new FromClauseStart(new SelectNode(null, expressionSpecs, null), fromClauseEngine);
    }

    @Override
    public FromClauseStart select(final ExpressionSpec... expressions) {
        return new FromClauseStart(new SelectNode(null, expressions, null), fromClauseEngine);
    }

    @Override
    public <T> FromClauseStartTypeOverride<T> select(final TypeOverride<T> expression) {
        final ExpressionSpec[] expressionSpecs = switch (expression) {
            case TypeOverrideExpressionSpec<?> typeOverrideExpression -> new ExpressionSpec[]{typeOverrideExpression};
            case ConvertIntent<T> convertIntent -> convertIntent.target();
        };

        return new FromClauseStartTypeOverride<>(expression.returnType(), new SelectNode(null, expressionSpecs, expression.returnType()), fromClauseEngine);
    }

    @Override
    public FromClauseStart select() {
        return new FromClauseStart(new SelectNode(null, new ExpressionSpec[0], null), fromClauseEngine);
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
        final DtoDeletor<DTO> dtoDtoDeletor = new DtoDeletor<>(dtoClass,
                ormTable,
                createDtoLitebridgeContext());
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
        final SqlDeletor sqlDeletor = new SqlDeletor(new Table(tableName), null, createSqlLitebridgeContext());
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

    public UpdateResult mergeInto(final String tableName, final Function<SqlMergeUsingStep, MergeTerminal> merge) {
        final MergeEngine mergeEngine = new MergeEngine(createSqlLitebridgeContext());
        return mergeEngine.mergeInto(tableName, merge);
    }

    public <DTO> UpdateResult mergeInto(final Class<DTO> dtoClass, final Function<DtoMergeUsingStep<DTO>, MergeTerminal> merge) {
        final MergeEngine mergeEngine = new MergeEngine(createDtoLitebridgeContext());
        return mergeEngine.mergeInto(dtoClass, merge);
    }

    /**
     * Provides access to execute native/raw SQL queries.
     * <p>
     * These types of queries/statements bypass most of the ORM layers and returns the low-level result.
     *
     * @return the native SQL execution API
     */
    public NativeSqlContext nativeSql() {
        return nativeSqlContext;
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
        final DtoSelectSpec selectSpec = new DtoSelectSpec(dtoClass, ormTable, new NoOpAliasGenerator(), createDtoLitebridgeContext());
        selectSpec.setExpressions(row.columnStream()
                .map(rowColumn -> {
                    final FieldAccessor fieldAccessor = ormTable.getFieldForColumnName(rowColumn.column().name());
                    // Some database providers do not return the table schema in the results; compensate for that
                    final Column targetColumn = ormTable.getColumnMetaData(rowColumn.column().name()).toColumn();
                    return (ExpressionSpec) new SelectFieldSpec(fieldAccessor, targetColumn);
                })
                .toList());

        final SelectSpecDtoMapper selectSpecDtoMapper = new SelectSpecDtoMapper(selectSpec, databaseProvider.getTypeConverter(), tableRegistry, dtoConstructor, createDtoLitebridgeContext());

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
     * @param <DTO>             the type of the DTO
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

    QueryPlanCache queryPlanCache() {
        return queryPlanCache;
    }

    private LitebridgeContext createDtoLitebridgeContext() {
        return createLitebridgeContext(LitebridgeContext.Mode.DTO);
    }

    private LitebridgeContext createSqlLitebridgeContext() {
        return createLitebridgeContext(LitebridgeContext.Mode.SQL);
    }

    private LitebridgeContext createLitebridgeContext(final LitebridgeContext.Mode mode) {
        final AliasGenerator aliasGenerator = new DefaultAliasGenerator(databaseProvider.getAliasTransformer());
        return new LitebridgeContext(mode, litebridgeConfig, databaseProvider, fromClauseEngine, queryPlanCache, aliasGenerator, tableMetaDataCache);
    }
}
