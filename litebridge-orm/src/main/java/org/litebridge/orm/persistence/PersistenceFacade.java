package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.ClassUtils;
import org.litebridge.commons.CollectionUtils;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.MappedFieldTarget;
import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.expression.BindValueExpression;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.db.spi.query.UpdateMetaData;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.db.spi.tx.TransactionManager;
import org.litebridge.db.spi.update.Delete;
import org.litebridge.db.spi.update.Insert;
import org.litebridge.db.spi.update.InsertResult;
import org.litebridge.db.spi.update.Update;
import org.litebridge.db.spi.update.UpdateResult;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.QueryBindValueExtractor;
import org.litebridge.orm.engine.QueryPlanCache;
import org.litebridge.orm.engine.ast.ConditionNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.expression.select.SelectColumnSpec;
import org.litebridge.orm.persistence.manytomany.NoOpFieldAccessor;
import org.litebridge.tracking.ChangeTracker;
import org.litebridge.tracking.ChangedCollectionField;
import org.litebridge.tracking.ChangedField;
import org.litebridge.tracking.ChangedFields;
import org.litebridge.tracking.ClassFieldAccessorCache;
import org.litebridge.tracking.FieldAccessor;
import org.litebridge.tracking.TrackedDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

/**
 * The PersistenceFacade class provides an abstraction layer for managing the persistence
 * of Data Transfer Objects (DTOs) within a database system. It simplifies interactions
 * with the database, supporting operations such as saving, inserting, and updating
 * records while ensuring consistency with the underlying ORM table definitions.
 * <p>
 * This class relies on a TableRegistry for mapping DTOs to their corresponding ORM
 * tables and a DatabaseProvider for executing database statements. It encapsulates
 * complex operations, including statement preparation, dependency resolution, primary
 * key updates, and transaction management.
 */
public class PersistenceFacade {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceFacade.class);
    private static final UpdateResult EMPTY_UPDATE_RESULT = new UpdateResult(0);
    private static final NoOpStatementBuilder NO_OP_STATEMENT_BUILDER = new NoOpStatementBuilder();

    private final TableProvider tableProvider;
    private final TransactionalDatabaseProvider databaseProvider;
    private final TransactionManager transactionManager;
    private final ChangeTracker changeTracker;
    private final ClassFieldAccessorCache classFieldAccessorCache;
    private final DtoConstructor dtoConstructor;
    private final LitebridgeContext litebridgeContext;

    /**
     * Constructs a new {@code PersistenceFacade} instance.
     *
     * @param tableRegistry     the registry of ORM tables
     * @param databaseProvider  the database provider
     * @param changeTracker     the change tracker
     * @param dtoConstructor    the DTO constructor
     * @param litebridgeContext the Litebridge context
     */
    public PersistenceFacade(final TableRegistry tableRegistry,
                             final TransactionalDatabaseProvider databaseProvider,
                             final ChangeTracker changeTracker,
                             final DtoConstructor dtoConstructor,
                             final LitebridgeContext litebridgeContext) {
        this.tableProvider = new TableProvider(tableRegistry);
        this.databaseProvider = databaseProvider;
        this.transactionManager = databaseProvider.transactionManager();
        this.changeTracker = changeTracker;
        this.classFieldAccessorCache = changeTracker.classFieldAccessorCache();
        this.dtoConstructor = dtoConstructor;
        this.litebridgeContext = litebridgeContext;
    }

    /**
     * Saves a collection of Data Transfer Objects (DTOs) to the database. Each DTO in the
     * collection is processed sequentially, and the save operation determines whether to
     * insert or update the DTO based on its current state. If the operation generates
     * primary keys (e.g., in the case of an insert), those keys are updated in the respective DTOs.
     *
     * @param dtos  the collection of Data Transfer Objects to be saved in the database.
     *              Each DTO must correspond to a registered ORM table.
     * @param <DTO> the type of the DTOs
     * @throws SQLException if a database access error occurs during any of the save operations.
     */
    public <DTO> void save(final Collection<DTO> dtos) throws SQLException {
        for (DTO dto : dtos) {
            save(dto);
        }
    }

    /**
     * Saves the given Data Transfer Object (DTO) to the database. This method determines
     * whether the DTO should be inserted or updated based on its current state. If the
     * operation generates primary keys (e.g., in the case of an insert), those keys
     * are updated in the DTO.
     *
     * @param dto   the Data Transfer Object to be saved in the database. It must
     *              correspond to a registered ORM table.
     * @param <DTO> the type of the DTO
     * @throws SQLException if a database access error occurs during the save operation.
     */
    public <DTO> void save(DTO dto) throws SQLException {
        final StatementBuilder statementBuilder = createStatementBuilder(dto, new HashSet<>());
        final CompositeUpdateResult compositeUpdateResult = new CompositeUpdateResult();
        executeUpdateStatement(dto, null, statementBuilder, compositeUpdateResult);

        compositeUpdateResult.results().forEach(dtoUpdateResult -> {
            updateOneToManyReverseMappings(dtoUpdateResult, compositeUpdateResult);

            if (dtoUpdateResult.getUpdateResult() instanceof InsertResult insertResult
                    && !CollectionUtils.isEmpty(insertResult.generatedKeys())) {
                dtoUpdateResult.setDto(updateDtoPrimaryKey(dtoUpdateResult.getDto(), insertResult.generatedKeys()));
            } else {
                tableProvider.getTableOrThrow(dtoUpdateResult.getDto().getClass()).syncPersistedDto(dtoUpdateResult.getDto());
            }
        });
    }

    /**
     * Inserts the specified Data Transfer Object (DTO) into the database.
     * This method constructs an SQL insert statement based on the provided DTO
     * and executes it. If the insertion generates primary keys, those keys
     * will be updated in the corresponding fields of the DTO.
     *
     * @param dto the Data Transfer Object to be inserted into the database.
     *            It must correspond to a registered ORM table.
     * @throws SQLException if a database access error occurs during the insertion process.
     */
    public void insert(final Object dto) throws SQLException {
        final StatementBuilder statementBuilder = createInsertBuilder(dto, tableProvider.getTableOrThrow(dto.getClass()), new HashSet<>());
        final CompositeUpdateResult compositeUpdateResult = new CompositeUpdateResult();
        executeUpdateStatement(dto, null, statementBuilder, compositeUpdateResult);

        compositeUpdateResult.results().forEach(dtoUpdateResult -> {
            updateOneToManyReverseMappings(dtoUpdateResult, compositeUpdateResult);

            if (dtoUpdateResult.getUpdateResult() instanceof InsertResult insertResult) {
                if (!CollectionUtils.isEmpty(insertResult.generatedKeys())) {
                    updateDtoPrimaryKey(dtoUpdateResult.getDto(), insertResult.generatedKeys());
                }
            } else {
                tableProvider.getTableOrThrow(dtoUpdateResult.getDto().getClass()).syncPersistedDto(dtoUpdateResult.getDto());
            }
        });
    }

    /**
     * Updates the specified Data Transfer Object (DTO) in the corresponding database table.
     * This method constructs an SQL update statement based on the provided DTO
     * and executes it. The update operation modifies rows in the database that match
     * the DTO's conditions, such as its primary key or other specified filters.
     *
     * @param dto the Data Transfer Object to be updated in the database.
     *            It must correspond to a registered ORM table and include valid
     *            fields or conditions to perform the update.
     * @throws SQLException if a database access error occurs during the update process.
     */
    public void update(final Object dto) throws SQLException {
        final StatementBuilder statementBuilder = createUpdateBuilder(dto, tableProvider.getTableOrThrow(dto.getClass()), new HashSet<>());
        executeUpdateStatement(dto, null, statementBuilder, new CompositeUpdateResult());
    }

    /**
     * Deletes the specified Data Transfer Object (DTO) from the corresponding database table.
     *
     * @param dto the Data Transfer Object to be deleted from the database
     * @throws SQLException if a database access error occurs during the delete process
     */
    public void delete(final Object dto) throws SQLException {
        final StatementBuilder statementBuilder = createDeleteBuilder(dto, tableProvider.getTableOrThrow(dto.getClass()), new HashSet<>());
        executeUpdateStatement(dto, null, statementBuilder, new CompositeUpdateResult());
    }

    private StatementBuilder createInsertBuilder(final Object dto, final OrmTable table, final Set<Object> inProgressDtos) {
        final InsertBuilder insertBuilder = new InsertBuilder(table, litebridgeContext);

        if (prepareUpdateStatement(dto, table, insertBuilder, inProgressDtos) == null) {
            return NO_OP_STATEMENT_BUILDER;
        }

        return insertBuilder;
    }

    private StatementBuilder createUpdateBuilder(final Object dto, final OrmTable table, final Set<Object> inProgressDtos) {
        final UpdateBuilder updateBuilder = new UpdateBuilder(table, litebridgeContext);

        if (prepareUpdateStatement(dto, table, updateBuilder, inProgressDtos) == null) {
            return NO_OP_STATEMENT_BUILDER;
        }

        return updateBuilder;
    }

    private StatementBuilder createDeleteBuilder(final Object dto, final OrmTable table, final Set<Object> inProgressDtos) {
        final DeleteBuilder deleteBuilder = new DeleteBuilder(table, litebridgeContext);
        prepareDeleteStatement(dto, table, deleteBuilder, inProgressDtos);
        return deleteBuilder;
    }

    private <DTO> @Nullable StatementChain prepareUpdateStatement(final DTO dto, final OrmTable table, final AbstractStatementBuilder statementBuilder, final Set<Object> inProgressDtos) {
        inProgressDtos.add(dto);

        final boolean isInsert = statementBuilder instanceof InsertBuilder;
        TrackedDto<?> trackedDto = changeTracker.getTrackedDtoOrNull(dto);

        if (trackedDto == null) {
            if (isInsert) {
                // If it's an insert, we want all fields to be considered changed
                changeTracker.trackDtoFields(dto, new HashSet<>(classFieldAccessorCache.fieldAccessors(dto.getClass())), true);
                trackedDto = changeTracker.getTrackedDto(dto);
            } else {
                trackedDto = table.ensureTrackedDto(dto);
            }
        }

        final ChangedFields changedFields = trackedDto.changedFields();

        if (LOGGER.isTraceEnabled()) {
            final StringJoiner sj = new StringJoiner(", ", "[", "]");
            changedFields.forEach(changedField -> sj.add(changedField.name() + "=" + changedField.value()));
            LOGGER.trace("Changed fields for DTO: {}: {}", dto, sj);
        }

        final StatementChain statementChain = statementBuilder.statementChain();
        boolean columnsAdded = false;
        final LinkedHashMap<String, Object> insertValues = isInsert ? new LinkedHashMap<>() : null;

        for (Map.Entry<FieldAccessor, MappedFieldTarget> entry : table.mappedFieldTargets()) {
            final FieldAccessor fieldAccessor = entry.getKey();

            if (fieldAccessor instanceof NoOpFieldAccessor) {
                LOGGER.trace("Skipping NoOpFieldAccessor for mapped target '{}'", entry.getValue());
                continue;
            }

            if (entry.getValue() instanceof MappedManyToMany mappedManyToMany) {
                // Collection of other DTOs (reverse-mapped collection)
                processManyToManyUpdate(dto, table, inProgressDtos, mappedManyToMany, changedFields, fieldAccessor, statementChain);
                continue;
            } else if (entry.getValue() instanceof MappedOneToMany mappedOneToMany) {
                // Collection of other DTOs (reverse-mapped collection)
                processOneToManyUpdate(dto, table, inProgressDtos, mappedOneToMany, statementChain);
                continue;
            }

            final ChangedField changedField = changedFields.getOrNull(fieldAccessor.name());

            if (changedField == null) {
                continue;
            }

            final Object value = changedField.value();
            //TODO: optimise basic type check; add to FieldAccessor as metadata perhaps?
            final boolean basicType = ClassUtils.isBasicType(fieldAccessor.type());

            if (basicType) {
                if (statementBuilder instanceof UpdateBuilder updateBuilder) {
                    updateBuilder.setField(fieldAccessor.name(), value);
                } else {
                    insertValues.put(fieldAccessor.name(), value);
                }
                columnsAdded = true;
            } else {
                // Dealing with an embedded DTO - add the context to the table provider
                tableProvider.pushContext(table.getContextTableRegistry());
                final ColumnMetaData columnMetaData = (ColumnMetaData) entry.getValue();

                try {
                    final OrmTable nestedDtoTable = tableProvider.getTableOrThrow(Objects.requireNonNull(value).getClass());

                    if (!nestedDtoTable.isPersistedDto(value)) {
                        // Cascade save to the nested DTO
                        final PipedStatement existingStatement = statementChain.getDependency(value);

                        if (existingStatement == null) {
                            // Check if the nested DTO's PK is set
                            final boolean dtoPkSet = nestedDtoTable.getMetaData().primaryKey().stream().anyMatch(pkColumn -> {
                                final FieldAccessor embeddedDtoPkAccessor = nestedDtoTable.getFieldForColumnName(pkColumn.name());
                                final Object embeddedDtoPkValue = embeddedDtoPkAccessor.get(value);
                                return !Objects.equals(embeddedDtoPkValue, ClassUtils.getDefaultValue(embeddedDtoPkAccessor.type()));
                            });

                            if (!inProgressDtos.contains(value)) {
                                // First time we're encountering this nested DTO; create an insert/update statement for it
                                final StatementBuilder dependencyStatementBuilder = createStatementBuilder(value, inProgressDtos);

                                if (!dtoPkSet) {
                                    // PK not yet set - pipe the generated key back to the parent DTO
                                    final PipedStatement dependencyPipe = new PipedStatement(dependencyStatementBuilder, value, updateResult -> {
                                        if (updateResult instanceof InsertResult insertResult
                                                && !CollectionUtils.isEmpty(insertResult.generatedKeys())) {

                                            insertResult.generatedKeys().forEach((pkColumn, pkValue) -> {

                                                if (columnMetaData.getJoinColumn() != null && columnMetaData.getJoinColumn().equals(pkColumn.name())) {
                                                    if (statementBuilder instanceof UpdateBuilder updateBuilder) {
                                                        updateBuilder.setField(fieldAccessor.name(), pkValue);
                                                    } else {
                                                        insertValues.put(fieldAccessor.name(), pkValue);
                                                    }
                                                }
                                            });
                                            updateDtoPrimaryKey(value, insertResult.generatedKeys());
                                        }
                                    });

                                    statementChain.addDependency(value, dependencyPipe);
                                } else {
                                    // PK already set - set the PK value on the current DTO and ensure the embedded DTO is persisted
                                    nestedDtoTable.getMetaData().primaryKey().forEach(pkColumn -> {
                                        final FieldAccessor embeddedDtoPkAccessor = nestedDtoTable.getFieldForColumnName(pkColumn.name());
                                        final Object embeddedDtoPkValue = embeddedDtoPkAccessor.get(value);

                                        if (columnMetaData.getJoinColumn() != null && columnMetaData.getJoinColumn().equals(pkColumn.name())) {
                                            if (statementBuilder instanceof UpdateBuilder updateBuilder) {
                                                updateBuilder.setField(fieldAccessor.name(), embeddedDtoPkValue);
                                            } else {
                                                insertValues.put(fieldAccessor.name(), embeddedDtoPkValue);
                                            }
                                        }
                                    });

                                    statementChain.addDependency(value, new PipedStatement(dependencyStatementBuilder, value));
                                }
                            } else {
                                // Statement for the nested DTO is under construction - pipe its PK to this field when available
                                if (!dtoPkSet) {
                                    // PK not yet set - pipe the generated key back to the parent DTO
                                    final PipedStatement dependencyPipe = new PipedStatement(NO_OP_STATEMENT_BUILDER, value, updateResult -> {
                                        if (updateResult instanceof InsertResult insertResult
                                                && !CollectionUtils.isEmpty(insertResult.generatedKeys())) {

                                            insertResult.generatedKeys().forEach((pkColumn, pkValue) -> {
                                                if (columnMetaData.getJoinColumn() != null && columnMetaData.getJoinColumn().equals(pkColumn.name())) {
                                                    if (statementBuilder instanceof UpdateBuilder updateBuilder) {
                                                        updateBuilder.setField(fieldAccessor.name(), pkValue);
                                                    } else {
                                                        insertValues.put(fieldAccessor.name(), pkValue);
                                                    }
                                                }
                                            });

                                            updateDtoPrimaryKey(value, insertResult.generatedKeys());
                                        }
                                    });

                                    statementChain.addDependency(value, dependencyPipe);
                                } else {
                                    // PK already set - set the PK value on the current DTO and ensure the embedded DTO is persisted
                                    nestedDtoTable.getMetaData().primaryKey().stream().forEach(pkColumn -> {
                                        final FieldAccessor embeddedDtoPkAccessor = nestedDtoTable.getFieldForColumnName(pkColumn.name());
                                        final Object embeddedDtoPkValue = embeddedDtoPkAccessor.get(value);
                                        if (columnMetaData.getJoinColumn() != null && columnMetaData.getJoinColumn().equals(pkColumn.name())) {
                                            if (statementBuilder instanceof UpdateBuilder updateBuilder) {
                                                updateBuilder.setField(fieldAccessor.name(), embeddedDtoPkValue);
                                            } else {
                                                insertValues.put(fieldAccessor.name(), embeddedDtoPkValue);
                                            }
                                        }
                                    });

                                    statementChain.addDependency(value, new PipedStatement(NO_OP_STATEMENT_BUILDER, value));
                                }
                            }
                        }
                    } else {
                        // Get the primary key
                        nestedDtoTable.getMetaData().primaryKey().forEach(pkColumn -> {
                            final FieldAccessor embeddedDtoPkAccessor = nestedDtoTable.getFieldForColumnName(pkColumn.name());
                            final Object embeddedDtoPkValue = embeddedDtoPkAccessor.get(value);
                            final Column joinColumn = table.columnMetaDataForField(fieldAccessor.name()).toColumn();

                            if (statementBuilder instanceof UpdateBuilder updateBuilder) {
                                updateBuilder.setField(fieldAccessor.name(), embeddedDtoPkValue);
                            } else {
                                insertValues.put(fieldAccessor.name(), embeddedDtoPkValue);
                            }
                        });
                    }
                } finally {
                    tableProvider.popContext();
                }
            }
        }

        if (isInsert) {
            final InsertBuilder insertBuilder = (InsertBuilder) statementBuilder;
            insertBuilder.addRow(insertValues);
        } else {
            if (!columnsAdded
                    && statementChain.getDependencies().isEmpty()
                    && statementChain.getDependants().isEmpty()) {
                return null;
            }

            final UpdateBuilder updateBuilder = (UpdateBuilder) statementBuilder;
            addPrimaryKeyConditions(dto, table, updateBuilder);
        }

        return statementChain;
    }

    private <DTO> StatementChain prepareDeleteStatement(final DTO dto, final OrmTable table, final DeleteBuilder deleteBuilder, final Set<Object> inProgressDtos) {
        inProgressDtos.add(dto);
        addPrimaryKeyConditions(dto, table, deleteBuilder);
        return deleteBuilder.statementChain();
    }

    private <DTO> void processOneToManyUpdate(final DTO dto, final OrmTable table, final Set<Object> inProgressDtos, final MappedOneToMany mappedOneToMany, final StatementChain statementChain) {
        final Collection<?> values = (Collection<?>) mappedOneToMany.collection().get(dto);

        if (!CollectionUtils.isEmpty(values)) {
            LOGGER.trace("Processing MappedOneToMany relationship '{}' of DTO: {}", mappedOneToMany.collection().name(), dto);
            final Class<?> collectionDtoClass = mappedOneToMany.collection().genericType();
            tableProvider.pushContext(table.getContextTableRegistry());
            try {
                final OrmTable collectionDtoTable = tableProvider.getTableOrThrow(collectionDtoClass);

                for (Object value : values) {
                    if (!collectionDtoTable.isPersistedDto(value) && !inProgressDtos.contains(value)) {
                        // Cascade save to the nested DTO
                        final PipedStatement existingStatement = statementChain.getDependency(value);

                        if (existingStatement == null) {
                            final StatementBuilder dependantStatementBuilder = createStatementBuilder(value, inProgressDtos);
                            statementChain.addDependant(value, new PipedStatement(dependantStatementBuilder, value, parentUpdateResult -> {
                                final List<ColumnMetaData> primaryKeyColumns = table.getMetaData().primaryKey();

                                if (primaryKeyColumns.size() != 1) {
                                    //TODO: add support for composite primary keys in one-to-many relationships
                                    throw new UnsupportedOperationException("Composite primary keys are not yet supported for one-to-many relationships; table: " + table.getMetaData().name());
                                }

                                if (parentUpdateResult instanceof InsertResult insertResult
                                        && !CollectionUtils.isEmpty(insertResult.generatedKeys())) {
                                    final Object pkValue = insertResult.generatedKeys().values().iterator().next();

                                    if (mappedOneToMany.mappedByField() != null) {
                                        dependantStatementBuilder.setField(mappedOneToMany.mappedByField().name(), pkValue);
                                    }
                                } else {
                                    final ColumnMetaData pkColumn = primaryKeyColumns.getFirst();
                                    final FieldAccessor pkField = table.getFieldForColumnName(pkColumn.name());

                                    if (mappedOneToMany.mappedByField() != null) {
                                        dependantStatementBuilder.setField(mappedOneToMany.mappedByField().name(), pkField.get(dto));
                                    }
                                }
                            }));
                        }
                    }
                }
            } finally {
                tableProvider.popContext();
            }
        }
    }

    private <DTO> void processManyToManyUpdate(final DTO leftDto, final OrmTable leftOrmTable, final Set<Object> inProgressDtos, final MappedManyToMany mappedManyToMany, final ChangedFields changedFields, final FieldAccessor fieldAccessor, final StatementChain statementChain) {
        final ChangedCollectionField changedCollectionField = (ChangedCollectionField) changedFields.get(fieldAccessor.name()).orElse(null);

        if (changedCollectionField != null && !changedCollectionField.updatedIndices().isEmpty()) {
            LOGGER.trace("Processing MappedManyToMany relationship '{}' of DTO: {}", mappedManyToMany.collection().name(), leftDto);
            final Class<?> collectionDtoClass = mappedManyToMany.collection().genericType();
            tableProvider.pushContext(leftOrmTable.getContextTableRegistry());

            try {
                final Collection<?> updatedValues = changedCollectionField.updatedValues();

                for (Object value : updatedValues) {
                    if (!inProgressDtos.contains(value)) {
                        // Prepare join table entry
                        final InsertBuilder joinTableInsertBuilder = new InsertBuilder(mappedManyToMany.joinOrmTable(), litebridgeContext);
                        statementChain.addDependant(joinTableInsertBuilder, new PipedStatement(joinTableInsertBuilder, value));

                        // Cascade save to the nested DTO
                        final PipedStatement existingStatement = statementChain.getDependency(value);

                        if (existingStatement == null) {
                            final StatementBuilder dependantStatementBuilder = createStatementBuilder(value, inProgressDtos);
                            statementChain.addDependency(value, new PipedStatement(dependantStatementBuilder, value, updateResult -> {
                                if (updateResult instanceof InsertResult insertResult
                                        && !CollectionUtils.isEmpty(insertResult.generatedKeys())) {
                                    updateDtoPrimaryKey(value, insertResult.generatedKeys());
                                }

                                // Add join table entry
                                final LinkedHashMap<String, @Nullable Object> joinTableInsertValues = new LinkedHashMap<>();
                                addManyToManyJoinValue(leftDto, mappedManyToMany.joinColumn(), joinTableInsertValues);
                                addManyToManyJoinValue(value, mappedManyToMany.inverseJoinColumn(), joinTableInsertValues);
                                joinTableInsertBuilder.addRow(joinTableInsertValues);
                            }));
                        }
                    }
                }
            } finally {
                tableProvider.popContext();
            }
        }
    }

    private Object updateDtoPrimaryKey(final Object dto, final Map<ColumnMetaData, Object> generatedKeys) {
        Object currentDto = dto;
        final OrmTable embeddedDtoTable = tableProvider.getTableOrThrow(dto.getClass());
        final Map<FieldAccessor, @Nullable Object> currentPkValues = new HashMap<>();
        final Map<FieldAccessor, Object> generatedPkValues = new HashMap<>();

        for (ColumnMetaData pkColumn : generatedKeys.keySet()) {
            final FieldAccessor field = embeddedDtoTable.getFieldForColumnName(pkColumn.name());
            final Object currentPkValue = field.get(dto);
            currentPkValues.put(field, currentPkValue);

            if (Objects.equals(currentPkValue, ClassUtils.getDefaultValue(field.type()))) {
                final Object generatedKey = generatedKeys.get(pkColumn);
                final Object convertedValue = Objects.requireNonNull(databaseProvider.getTypeConverter().convert(generatedKey, field.type()));
                generatedPkValues.put(field, convertedValue);
            } else {
                LOGGER.trace("Generated key for DTO '{}' already set - ignoring; current value: {}", dto, currentPkValue);
            }
        }

        if (dto instanceof Record) {
            // Can't set a record's field - recreate the record
            final List<DtoConstructor.FieldAccessorValue> fieldAccessorValues = classFieldAccessorCache.fieldAccessors(dto.getClass()).stream()
                    .map(fieldAccessor -> {
                        if (generatedPkValues.containsKey(fieldAccessor)) {
                            return new DtoConstructor.FieldAccessorValue(fieldAccessor, generatedPkValues.get(fieldAccessor));
                        } else {
                            return new DtoConstructor.FieldAccessorValue(fieldAccessor, fieldAccessor.get(dto));
                        }
                    })
                    .toList();

            currentDto = DtoMapper.constructDto(dto.getClass(), fieldAccessorValues, dtoConstructor);
        } else {
            // Normal class
            generatedPkValues.forEach((field, value) -> {
                field.set(dto, value);
                transactionManager.addRollbackCallback(() -> {
                    LOGGER.trace("Rolling back generated key for DTO '{}'", dto);
                    field.set(dto, currentPkValues.get(field));
                    embeddedDtoTable.syncPersistedDto(dto);
                });
            });
        }

        embeddedDtoTable.syncPersistedDto(currentDto);
        return currentDto;
    }

    private void addManyToManyJoinValue(final Object dto, final String joinColumnName, final LinkedHashMap<String, @Nullable Object> rowValues) {
        final OrmTable ormTable = tableProvider.getTableOrThrow(dto.getClass());
        final List<ColumnMetaData> primaryKeyColumns = ormTable.getMetaData().primaryKey();

        if (primaryKeyColumns.size() != 1) {
            //TODO: add support for composite primary keys in many-to-many joins
            throw new UnsupportedOperationException("Composite primary keys are not yet supported for many-to-many relationships; table: " + ormTable.getMetaData().name());
        }

        final ColumnMetaData pkColumn = primaryKeyColumns.getFirst();
        final FieldAccessor pkField = ormTable.getFieldForColumnName(pkColumn.name());
        rowValues.put(joinColumnName, pkField.get(dto));
    }

    @SuppressWarnings("unchecked")
    private void updateOneToManyReverseMappings(final DtoUpdateResult dtoUpdateResult, final CompositeUpdateResult compositeUpdateResult) {
        tableProvider.pushContext(dtoUpdateResult, new HashSet<>());
        try {
            final Object dto = dtoUpdateResult.getDto();
            final OrmTable ormTable = tableProvider.getTableOrThrow(dto.getClass());

            if (!CollectionUtils.isEmpty(ormTable.getOneToManyReverseMappings())) {
                ormTable.getOneToManyReverseMappings().forEach(collectionField -> {
                    changeTracker.getTrackedDtos(collectionField.dtoClass())
                            .forEach(trackedDto -> {
                                final Collection<Object> collection = (Collection<Object>) collectionField.get(trackedDto.dto());

                                // If the collection does not exist yet, initialise it
                                if (collection == null) {
                                    final Collection<Object> newCollection = (Collection<Object>) ClassUtils.newInstance(collectionField.type());
                                    collectionField.set(trackedDto.dto(), newCollection);
                                    newCollection.add(dto);
                                    transactionManager.addRollbackCallback(() -> collectionField.set(trackedDto.dto(), null));
                                } else if (!collection.contains(dto)) {
                                    // Add the updated value to the collection
                                    LOGGER.trace("Adding DTO to reverse mapping collection '{}': {}", collectionField.name(), dto);
                                    collection.add(dto);
                                    transactionManager.addRollbackCallback(() -> collection.remove(dto));
                                }
                            });
                });
            }
        } finally {
            tableProvider.popContext();
        }
    }

    private StatementBuilder createStatementBuilder(final Object dto, final Set<Object> inProgressDtos) {
        if (inProgressDtos.contains(dto)) {
            throw new IllegalStateException("DTO already in progress: %s".formatted(dto));
        }

        final OrmTable table = tableProvider.getTableOrThrow(dto.getClass());

        if (table.isPersistedDto(dto)) {
            return createUpdateBuilder(dto, table, inProgressDtos);
        } else {
            return createInsertBuilder(dto, table, inProgressDtos);
        }
    }

    /**
     * Executes an update statement provided by the given {@code AbstractStatementBuilder}.
     * This method first resolves and executes all dependent piped statements, propagating
     * their results through the corresponding value pipes. It then builds and executes
     * the main update statement, which could be either an {@code Insert} or {@code Update},
     * using the configured {@code DatabaseProvider}.
     *
     * @param statementBuilder the builder for the update statement to be executed,
     *                         including any dependencies that need to be resolved beforehand
     * @return an {@code UpdateResult} representing the outcome of the executed statement,
     * including the number of rows affected
     * @throws SQLException if a database access error occurs during statement execution
     */
    private CompositeUpdateResult executeUpdateStatement(final Object dto,
                                                         final @Nullable DtoUpdateResult parentResult,
                                                         final StatementBuilder statementBuilder,
                                                         final CompositeUpdateResult result) throws SQLException {
        final DtoUpdateResult dtoUpdateResult = new DtoUpdateResult(dto, parentResult);

        if (statementBuilder instanceof NoOpStatementBuilder) {
            dtoUpdateResult.setUpdateResult(EMPTY_UPDATE_RESULT);
            result.add(dtoUpdateResult);
            return result;
        }

        for (Map.Entry<Object, PipedStatement> entry : statementBuilder.statementChain().getDependencies().entrySet()) {
            final PipedStatement pipedStatement = entry.getValue();

            if (pipedStatement.statementBuilder() instanceof NoOpStatementBuilder) {
                final DtoUpdateResult existing = result.getDtoUpdateResult(pipedStatement.dto());

                if (existing != null) {
                    pipedStatement.valuePipe().accept(existing.getUpdateResult());
                    continue;
                }
            }

            executeUpdateStatement(pipedStatement.dto(), dtoUpdateResult, pipedStatement.statementBuilder(), result);
            final DtoUpdateResult dependencyResult = result.getDtoUpdateResult(pipedStatement.dto());

            if (dependencyResult != null) {
                pipedStatement.valuePipe().accept(dependencyResult.getUpdateResult());
            }
        }

        final QueryNode node = statementBuilder.node();
        final int nodeHash = node.hashCode();
        final QueryPlanCache.CachedOperation cachedOperation = litebridgeContext.queryPlanCache().get(nodeHash);

        if (cachedOperation != null) {
            final List<@Nullable Object> rawBindValues = QueryBindValueExtractor.extractBindValues(node);
            final PreparedSql preparedSql = cachedOperation.preparedSql(rawBindValues);

            if (statementBuilder instanceof InsertBuilder) {
                dtoUpdateResult.setUpdateResult(databaseProvider.insert(preparedSql, transactionManager));
            } else if (statementBuilder instanceof UpdateBuilder) {
                dtoUpdateResult.setUpdateResult(databaseProvider.update(preparedSql, transactionManager));
            } else if (statementBuilder instanceof DeleteBuilder) {
                dtoUpdateResult.setUpdateResult(databaseProvider.delete(preparedSql, transactionManager));
            }
        } else {
            final PreparedOperation preparedOperation = statementBuilder.build();

            if (preparedOperation.operation() instanceof Update update && update.columns().isEmpty()) {
                dtoUpdateResult.setUpdateResult(new UpdateResult(0));
            } else {
                // Generate SQL and create type conversion metadata
                final String sql = databaseProvider.toSql(preparedOperation.operation(), databaseProvider.transactionManager());
                final UpdateMetaData updateMetaData = statementBuilder.createUpdateMetaData();
                // Cache compiled SQL for this AST
                final List<Integer> bindValueSqlTypes = preparedOperation.bindValues().stream()
                        .map(BindValue::sqlDataType)
                        .toList();
                litebridgeContext.queryPlanCache().put(nodeHash, new QueryPlanCache.CachedOperation(sql, bindValueSqlTypes, null, updateMetaData));

                // Execute SQL query
                final PreparedSql executionSql = new PreparedSql(sql, preparedOperation.bindValues(), null, updateMetaData);

                final UpdateResult updateResult = switch (preparedOperation.operation()) {
                    case Insert insert -> databaseProvider.insert(executionSql, transactionManager);
                    case Update update -> databaseProvider.update(executionSql, transactionManager);
                    case Delete delete -> databaseProvider.delete(executionSql, transactionManager);
                    default ->
                            throw new IllegalStateException("Unexpected operation type: " + preparedOperation.operation());
                };

                dtoUpdateResult.setUpdateResult(updateResult);
            }
        }

        result.add(dtoUpdateResult);

        for (Map.Entry<Object, PipedStatement> entry : statementBuilder.statementChain().getDependants().entrySet()) {
            final PipedStatement pipedStatement = entry.getValue();
            pipedStatement.valuePipe().accept(dtoUpdateResult.getUpdateResult());
            executeUpdateStatement(pipedStatement.dto(), dtoUpdateResult, pipedStatement.statementBuilder(), result);
        }

        return result;
    }

    private static class TableProvider {

        private final Deque<TableRegistry> contextStack = new ArrayDeque<>();

        private TableProvider(final TableRegistry rootTableRegistry) {
            contextStack.push(rootTableRegistry);
        }

        public void pushContext(final TableRegistry tableRegistry) {
            contextStack.push(tableRegistry);
        }

        private void pushContext(final DtoUpdateResult dtoUpdateResult, final Set<DtoUpdateResult> visitedResults) {
            if (visitedResults.contains(dtoUpdateResult)) {
                return;
            }

            visitedResults.add(dtoUpdateResult);

            if (dtoUpdateResult.getParentResult() != null) {
                pushContext(dtoUpdateResult.getParentResult(), visitedResults);
            }

            final OrmTable table = getTableOrThrow(dtoUpdateResult.getDto().getClass());
            pushContext(table.getContextTableRegistry());
        }

        public void popContext() {
            contextStack.pop();
        }

        public OrmTable getTableOrThrow(final Class<?> dtoClass) {
            final Iterator<TableRegistry> iterator = contextStack.iterator();

            while (iterator.hasNext()) {
                final TableRegistry tableRegistry = iterator.next();
                final OrmTable table;

                if (iterator.hasNext()) {
                    table = tableRegistry.getOrmTable(dtoClass);
                } else {
                    // Root table registry - if not found, throw an exception
                    table = tableRegistry.getOrmTableOrThrow(dtoClass);
                }

                if (table != null) {
                    return table;
                }
            }

            throw new IllegalArgumentException("No table found for DTO class: " + dtoClass);
        }
    }

    /**
     * Adds primary key conditions for the given DTO and table to an {@link UpdateBuilder} or {@link DeleteBuilder}.
     *
     * @param dto              the DTO to add primary key conditions for
     * @param table            the table corresponding to the DTO
     * @param statementBuilder the statement builder to add conditions to. Must be an {@link UpdateBuilder} or {@link DeleteBuilder}.
     * @param <DTO>            class of the DTO
     */
    private <DTO> void addPrimaryKeyConditions(final DTO dto, final OrmTable table, final AbstractConditionalStatementBuilder statementBuilder) {
        QueryNode conditionNode = null;
        boolean first = true;

        for (ColumnMetaData columnMetaData : table.getMetaData().primaryKey()) {
            final Column pkColumn = columnMetaData.toColumn();
            final FieldAccessor field = table.getFieldForColumnName(pkColumn.name());
            final Object pkValue = field.get(dto);
            final SelectColumnSpec pkColumnSpec = new SelectColumnSpec(pkColumn);

            final LogicOperator logicOperator = first ? LogicOperator.NOOP : LogicOperator.AND;

            if (pkValue != null) {
                conditionNode = new ConditionNode(conditionNode, logicOperator, null, pkColumnSpec, Operator.EQ, pkValue);
            } else {
                conditionNode = new ConditionNode(conditionNode, logicOperator, null, pkColumnSpec, Operator.IS_NULL, null);
            }

            first = false;
        }

        if (conditionNode != null) {
            statementBuilder.where(conditionNode);
        }
    }

    private BindValue createBindValue(final @Nullable Object rawValue, final ColumnMetaData columnMetaData, final TypeConverter typeConverter) {
        final Object convertedValue = typeConverter.convert(rawValue, columnMetaData.getDataType());
        return new BindValue(convertedValue, columnMetaData.getDataType());
    }

    private static BindValueExpression createBindValueExpression(final @Nullable Object value, final int index) {
        final int valueSize;

        if (value instanceof Collection<?> collection) {
            valueSize = collection.size();
        } else {
            valueSize = 1;
        }

        return new BindValueExpression(index, valueSize);
    }
}
