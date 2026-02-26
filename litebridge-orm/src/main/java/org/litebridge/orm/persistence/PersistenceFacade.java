package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.ClassUtils;
import org.litebridge.commons.CollectionUtils;
import org.litebridge.commons.ObjectUtils;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.MappedFieldTarget;
import org.litebridge.db.spi.query.Condition;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.db.spi.update.ColumnValue;
import org.litebridge.db.spi.update.Insert;
import org.litebridge.db.spi.update.InsertResult;
import org.litebridge.db.spi.update.RowValue;
import org.litebridge.db.spi.update.Update;
import org.litebridge.db.spi.update.UpdateResult;
import org.litebridge.db.spi.update.UpdateStatement;
import org.litebridge.orm.persistence.manytomany.NoOpFieldAccessor;
import org.litebridge.tracking.ChangedCollectionField;
import org.litebridge.tracking.ChangedField;
import org.litebridge.tracking.ChangedFields;
import org.litebridge.tracking.FieldAccessor;
import org.litebridge.tracking.TrackedDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
    private final TableProvider tableProvider;
    private final DatabaseProvider databaseProvider;

    public PersistenceFacade(final TableRegistry tableRegistry,
                             final DatabaseProvider databaseProvider) {
        this.tableProvider = new TableProvider(tableRegistry);
        this.databaseProvider = databaseProvider;
    }

    /**
     * Saves a collection of Data Transfer Objects (DTOs) to the database. Each DTO in the
     * collection is processed sequentially, and the save operation determines whether to
     * insert or update the DTO based on its current state. If the operation generates
     * primary keys (e.g., in the case of an insert), those keys are updated in the respective DTOs.
     *
     * @param dtos the collection of Data Transfer Objects to be saved in the database.
     *             Each DTO must correspond to a registered ORM table.
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
     * @param dto the Data Transfer Object to be saved in the database. It must
     *            correspond to a registered ORM table.
     * @throws SQLException if a database access error occurs during the save operation.
     */
    public <DTO> void save(DTO dto) throws SQLException {
        final AbstractStatementBuilder<?> statementBuilder = createStatementBuilder(dto, new HashSet<>());
        final CompositeUpdateResult compositeUpdateResult = executeUpdateStatement(dto, null, statementBuilder);

        compositeUpdateResult.results().forEach(dtoUpdateResult -> {
            updateOneToManyReverseMappings(dtoUpdateResult, compositeUpdateResult);

            if (dtoUpdateResult.getUpdateResult() instanceof InsertResult insertResult
                    && !CollectionUtils.isEmpty(insertResult.generatedKeys())) {
                // TODO: composite PK support
                updateDtoPrimaryKey(dtoUpdateResult.getDto(), insertResult.generatedKeys().getFirst());
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
        final InsertBuilder statementBuilder = createInsertBuilder(dto, tableProvider.getTableOrThrow(dto.getClass()), new HashSet<>());
        final CompositeUpdateResult compositeUpdateResult = executeUpdateStatement(dto, null, statementBuilder);

        compositeUpdateResult.results().forEach(dtoUpdateResult -> {
            updateOneToManyReverseMappings(dtoUpdateResult, compositeUpdateResult);

            if (dtoUpdateResult.getUpdateResult() instanceof InsertResult insertResult) {
                if (!CollectionUtils.isEmpty(insertResult.generatedKeys())) {
                    // TODO: composite PK support
                    updateDtoPrimaryKey(dtoUpdateResult.getDto(), insertResult.generatedKeys().getFirst());
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
        final UpdateBuilder statementBuilder = createUpdateBuilder(dto, tableProvider.getTableOrThrow(dto.getClass()), new HashSet<>());
        executeUpdateStatement(dto, null, statementBuilder);
    }

    private InsertBuilder createInsertBuilder(final Object dto, final OrmTable table, final Set<Object> inProgressDtos) {
        final InsertBuilder insertBuilder = new InsertBuilder(table);
        prepareUpdateStatement(dto, table, insertBuilder, inProgressDtos);
        return insertBuilder;
    }

    private UpdateBuilder createUpdateBuilder(final Object dto, final OrmTable table, final Set<Object> inProgressDtos) {
        final UpdateBuilder updateBuilder = new UpdateBuilder(table);
        prepareUpdateStatement(dto, table, updateBuilder, inProgressDtos);
        return updateBuilder;
    }

    private <DTO> @Nullable StatementChain prepareUpdateStatement(final DTO dto, final OrmTable table, final AbstractStatementBuilder<?> statementBuilder, final Set<Object> inProgressDtos) {
        inProgressDtos.add(dto);
        final TrackedDto<?> trackedDto = table.ensureTrackedDto(dto);
        final ChangedFields changedFields = trackedDto.changedFields();

        if (changedFields.isEmpty()) {
            LOGGER.debug("No changed fields found for DTO: {}", dto);
            return null;
        }

        if (LOGGER.isTraceEnabled()) {
            final StringJoiner sj = new StringJoiner(", ", "[", "]");
            changedFields.forEach(changedField -> sj.add(changedField.name() + "=" + changedField.value()));
            LOGGER.trace("Changed fields for DTO: {}: {}", dto, sj);
        }

        final StatementChain statementChain = statementBuilder.statementChain();
        final List<ColumnValue> columnValues = new ArrayList<>();
        final boolean isInsert = statementBuilder instanceof InsertBuilder;


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
                processOneToManyUpdate(dto, table, inProgressDtos, mappedOneToMany, statementChain, columnValues);
                continue;
            }

            final ColumnMetaData column = (ColumnMetaData) entry.getValue();
            final ChangedField changedField = changedFields.getOrNull(fieldAccessor.name());
            final Object value;
            final boolean basicType;

            if (changedField == null) {
                if (isInsert && (column.isAutoIncrement() || column.getSequence() != null)) {
                    basicType = true;
                    value = null;
                } else {
                    continue;
                }
            } else {
                basicType = ClassUtils.isBasicType(changedField.value().getClass());
                value = changedField.value();
            }

            if (basicType) {
                columnValues.add(new ColumnValue(column, value));
            } else {
                // Dealing with an embedded DTO - add the context to the table provider
                tableProvider.pushContext(table.getContextTableRegistry());
                final OrmTable nestedDtoTable = tableProvider.getTableOrThrow(value.getClass());

                if (!nestedDtoTable.isPersistedDto(value)) {
                    // Cascade save to the embedded DTO
                    final PipedStatement existingStatement = statementChain.getDependency(value);

                    if (existingStatement == null) {
                        // Check if the nested DTO's PK is set
                        // TODO: composite PK support
                        final FieldAccessor embeddedDtoPkAccessor = nestedDtoTable.getFieldForColumnName(nestedDtoTable.getMetaData().primaryKey().getFirst().name());
                        final Object embeddedDtoPkValue = embeddedDtoPkAccessor.get(value);

                        if (!inProgressDtos.contains(value)) {
                            // Create an insert/update statement for the nested DTO
                            final AbstractStatementBuilder<?> dependencyStatementBuilder = createStatementBuilder(value, inProgressDtos);

                            if (embeddedDtoPkValue == null) {
                                // PK not yet set - pipe the generated key back to the parent DTO
                                final PipedStatement dependencyPipe = new PipedStatement(dependencyStatementBuilder, value, updateResult -> {
                                    if (updateResult instanceof InsertResult insertResult
                                            && !CollectionUtils.isEmpty(insertResult.generatedKeys())) {
                                        // TODO: composite PK support
                                        final Object pkValue = insertResult.generatedKeys().getFirst();
                                        columnValues.add(new ColumnValue(column, pkValue));
                                        updateDtoPrimaryKey(value, pkValue);
                                    }
                                });
                                statementChain.addDependency(value, dependencyPipe);
                            } else {
                                // PK already set - set the PK value on the current DTO and ensure the embedded DTO is persisted
                                columnValues.add(new ColumnValue(column, embeddedDtoPkValue));
                                statementChain.addDependency(value, new PipedStatement(dependencyStatementBuilder, value));
                            }
                        } else {
                            // Statement for the nested DTO is under construction - pipe its PK to this field when available
                            if (embeddedDtoPkValue == null) {
                                // PK not yet set - pipe the generated key back to the parent DTO
                                final PipedStatement dependencyPipe = new PipedStatement(new NoOpStatementBuilder(), value, updateResult -> {
                                    if (updateResult instanceof InsertResult insertResult
                                            && !CollectionUtils.isEmpty(insertResult.generatedKeys())) {
                                        // TODO: composite PK support
                                        final Object pkValue = insertResult.generatedKeys().getFirst();
                                        columnValues.add(new ColumnValue(column, pkValue));
                                        updateDtoPrimaryKey(value, pkValue);
                                    }
                                });
                                statementChain.addDependency(value, dependencyPipe);
                            } else {
                                // PK already set - set the PK value on the current DTO and ensure the embedded DTO is persisted
                                columnValues.add(new ColumnValue(column, embeddedDtoPkValue));
                                statementChain.addDependency(value, new PipedStatement(new NoOpStatementBuilder(), value));
                            }

                        }

                        tableProvider.popContext();
                    }
                } else {
                    // Get the primary key
                    final List<ColumnMetaData> embeddedDtoPk = nestedDtoTable.getMetaData().primaryKey();
                    // TODO: composite PK support
                    final FieldAccessor field = nestedDtoTable.getFieldForColumnName(embeddedDtoPk.get(0).name());
                    final Object pkValue = field.get(value);
                    columnValues.add(new ColumnValue(column, pkValue));
                }
            }
        }

//        for (ColumnMetaData column : table.getMetaData().columns()) {
//            final String fieldName = table.getFieldForColumnName(column.name()).name();
//            final ChangedField changedField = changedFields.getOrNull(fieldName);
//            final Object value;
//            final boolean basicType;
//
//            if (changedField == null) {
//                if (isInsert && (column.isAutoIncrement() || column.getSequence() != null)) {
//                    basicType = true;
//                    value = null;
//                } else {
//                    continue;
//                }
//            } else {
//                basicType = ClassUtils.isBasicType(changedField.value().getClass());
//                value = changedField.value();
//            }
//
//            if (basicType) {
//                columnValues.add(new ColumnValue(column, value));
//            } else {
//                // Dealing with an embedded DTO - add the context to the table provider
//                tableProvider.pushContext(table.getContextTableRegistry());
//                final OrmTable nestedDtoTable = tableProvider.getTableOrThrow(value.getClass());
//
//                if (!nestedDtoTable.isPersistedDto(value)) {
//                    // Cascade save to the embedded DTO
//                    final PipedStatement existingStatement = statementChain.getDependency(value);
//
//                    if (existingStatement == null) {
//                        final AbstractStatementBuilder<?> dependencyStatementBuilder = createStatementBuilder(value);
//
//                        // Check if the nested DTO's PK is set
//                        // TODO: composite PK support
//                        final FieldAccessor embeddedDtoPkAccessor = nestedDtoTable.getFieldForColumnName(nestedDtoTable.getMetaData().primaryKey().getFirst().name());
//                        final Object embeddedDtoPkValue = embeddedDtoPkAccessor.get(value);
//
//                        if (embeddedDtoPkValue == null) {
//                            // PK not yet set - pipe the generated key back to the parent DTO
//                            final PipedStatement dependencyPipe = new PipedStatement(dependencyStatementBuilder, value, updateResult -> {
//                                if (updateResult instanceof InsertResult insertResult
//                                        && !CollectionUtils.isEmpty(insertResult.generatedKeys())) {
//                                    // TODO: composite PK support
//                                    final Object pkValue = insertResult.generatedKeys().getFirst();
//                                    columnValues.add(new ColumnValue(column, pkValue));
//                                    updateDtoPrimaryKey(value, pkValue);
//                                }
//                            });
//                            statementChain.addDependency(value, dependencyPipe);
//                        } else {
//                            // PK already set - set the PK value on the current DTO and ensure the embedded DTO is persisted
//                            columnValues.add(new ColumnValue(column, embeddedDtoPkValue));
//                            statementChain.addDependency(value, new PipedStatement(dependencyStatementBuilder, value));
//                        }
//
//                        tableProvider.popContext();
//                    }
//                } else {
//                    // Get the primary key
//                    final List<ColumnMetaData> embeddedDtoPk = nestedDtoTable.getMetaData().primaryKey();
//                    // TODO: composite PK support
//                    final FieldAccessor field = nestedDtoTable.getFieldForColumnName(embeddedDtoPk.get(0).name());
//                    final Object pkValue = field.get(value);
//                    columnValues.add(new ColumnValue(column, pkValue));
//                }
//            }
//        }

        if (statementBuilder instanceof InsertBuilder insertBuilder) {
            insertBuilder.add(new DtoRowValue(dto, new RowValue(columnValues)));
        } else {
            final UpdateBuilder updateBuilder = (UpdateBuilder) statementBuilder;
            updateBuilder.setColumnValues(columnValues);

            table.getMetaData().primaryKey().forEach(pkColumn -> {
                final FieldAccessor field = table.getFieldForColumnName(pkColumn.name());
                final Object pkValue = field.get(dto);
                final Condition condition;

                if (pkValue != null) {
                    condition = new Condition(pkColumn, Operator.EQ, pkValue);
                } else {
                    condition = new Condition(pkColumn, Operator.IS_NULL);
                }

                updateBuilder.where(condition);
            });
        }

        return statementChain;
    }

    private <DTO> void processOneToManyUpdate(final DTO dto, final OrmTable table, final Set<Object> inProgressDtos, final MappedOneToMany mappedOneToMany, final StatementChain statementChain, final List<ColumnValue> columnValues) {
        final Collection<?> values = (Collection<?>) mappedOneToMany.collection().get(dto);

        if (!CollectionUtils.isEmpty(values)) {
            LOGGER.trace("Processing MappedOneToMany relationship '{}' of DTO: {}", mappedOneToMany.collection().name(), dto);
            final Class<?> collectionDtoClass = mappedOneToMany.collection().genericType();
            tableProvider.pushContext(table.getContextTableRegistry());
            final OrmTable collectionDtoTable = tableProvider.getTableOrThrow(collectionDtoClass);

            for (Object value : values) {
                if (!collectionDtoTable.isPersistedDto(value) && !inProgressDtos.contains(value)) {
                    // Cascade save to the nested DTO
                    final PipedStatement existingStatement = statementChain.getDependency(value);

                    if (existingStatement == null) {
                        final AbstractStatementBuilder<?> dependantStatementBuilder = createStatementBuilder(value, inProgressDtos);
                        statementChain.addDependant(value, new PipedStatement(dependantStatementBuilder, value, updateResult -> {
                            if (updateResult instanceof InsertResult insertResult
                                    && !CollectionUtils.isEmpty(insertResult.generatedKeys())) {
                                // TODO: composite PK support
                                final Object pkValue = insertResult.generatedKeys().getFirst();
                                updateDtoPrimaryKey(dto, pkValue);
                                final ColumnMetaData column = collectionDtoTable.getColumnForFieldName(mappedOneToMany.mappedByField().name());
                                columnValues.add(new ColumnValue(column, pkValue));
                            }
                        }));
                    }
                }
            }

            tableProvider.popContext();
        }
    }

    private <DTO> void processManyToManyUpdate(final DTO dto, final OrmTable table, final Set<Object> inProgressDtos, final MappedManyToMany mappedManyToMany, final ChangedFields changedFields, final FieldAccessor fieldAccessor, final StatementChain statementChain) {
        final ChangedCollectionField changedCollectionField = (ChangedCollectionField) changedFields.get(fieldAccessor.name()).orElse(null);

        if (changedCollectionField != null && !changedCollectionField.updatedIndices().isEmpty()) {
            LOGGER.trace("Processing MappedManyToMany relationship '{}' of DTO: {}", mappedManyToMany.collection().name(), dto);
            final Class<?> collectionDtoClass = mappedManyToMany.collection().genericType();
            tableProvider.pushContext(table.getContextTableRegistry());
            final OrmTable collectionDtoTable = tableProvider.getTableOrThrow(collectionDtoClass);
            final Collection<?> updatedValues = changedCollectionField.updatedValues();

            for (Object value : updatedValues) {
                if (!inProgressDtos.contains(value)) {
                    // Prepare join table entry
                    final InsertBuilder joinTableInsertBuilder = new InsertBuilder(mappedManyToMany.joinTable());
                    statementChain.addDependant(joinTableInsertBuilder, new PipedStatement(joinTableInsertBuilder, value));

                    // Cascade save to the nested DTO
                    final PipedStatement existingStatement = statementChain.getDependency(value);

                    if (existingStatement == null) {
                        final AbstractStatementBuilder<?> dependantStatementBuilder = createStatementBuilder(value, inProgressDtos);
                        statementChain.addDependency(value, new PipedStatement(dependantStatementBuilder, value, updateResult -> {
                            if (updateResult instanceof InsertResult insertResult
                                    && !CollectionUtils.isEmpty(insertResult.generatedKeys())) {
                                // TODO: composite PK support
                                final Object pkValue = insertResult.generatedKeys().getFirst();
                                updateDtoPrimaryKey(value, pkValue);
                            }

                            // Add join table entry
                            final List<ColumnValue> joinTableColumnValues = new ArrayList<>(dtoPrimaryKeyColumnValues(dto));
                            joinTableColumnValues.addAll(dtoPrimaryKeyColumnValues(value));
                            joinTableInsertBuilder.add(new DtoRowValue(mappedManyToMany.joinTable().dtoClass(), new RowValue(joinTableColumnValues)));
                        }));
                    }
                }
            }

            tableProvider.popContext();
        }
    }

    private void updateDtoPrimaryKey(final Object dto, final Object generatedKey) {
        final OrmTable embeddedDtoTable = tableProvider.getTableOrThrow(dto.getClass());
        final List<ColumnMetaData> embeddedDtoPk = embeddedDtoTable.getMetaData().primaryKey();
        // TODO: composite PK support
        final ColumnMetaData pkColumn = embeddedDtoPk.get(0);
        final FieldAccessor field = embeddedDtoTable.getFieldForColumnName(pkColumn.name());

        if (field.get(dto) == null) {
            final Object convertedValue = databaseProvider.getTypeConverter().convert(generatedKey, pkColumn.getDataType());
            field.set(dto, convertedValue);
            embeddedDtoTable.syncPersistedDto(dto);
        } else {
            LOGGER.trace("Generated key for DTO '{}' already set - ignoring", dto);
        }
    }

    private List<ColumnValue> dtoPrimaryKeyColumnValues(final Object dto) {
        final OrmTable embeddedDtoTable = tableProvider.getTableOrThrow(dto.getClass());
        final List<ColumnMetaData> embeddedDtoPk = embeddedDtoTable.getMetaData().primaryKey();
        // TODO: composite PK support
        final ColumnMetaData pkColumn = embeddedDtoPk.get(0);
        final FieldAccessor field = embeddedDtoTable.getFieldForColumnName(pkColumn.name());
        return List.of(new ColumnValue(pkColumn, field.get(dto)));
    }

    @SuppressWarnings("unchecked")
    private void updateOneToManyReverseMappings(final DtoUpdateResult dtoUpdateResult, final CompositeUpdateResult compositeUpdateResult) {
        tableProvider.pushContext(dtoUpdateResult, new HashSet<>());
        final Object dto = dtoUpdateResult.getDto();

        final OrmTable table = tableProvider.getTableOrThrow(dto.getClass());
        final List<MappedOneToMany> mappedOneToManyList = table.getOneToManyMappings();

        if (CollectionUtils.isEmpty(mappedOneToManyList)) {
            // No reverse mappings to update
            return;
        }

        mappedOneToManyList.forEach(mappedOneToMany -> {
            LOGGER.trace("Updating reverse mapping for field '{}' of DTO: {}", mappedOneToMany.collection().name(), dto);
            // Get the current value of the mapping
            final FieldAccessor reverseMappingCollection = mappedOneToMany.collection();
            final Collection<Object> currentCollection;
            final Collection<Object> dtoCollection = (Collection<Object>) reverseMappingCollection.get(dto);

            if (dtoCollection != null) {
                currentCollection = dtoCollection;
            } else {
                currentCollection = (Collection<Object>) ClassUtils.newInstance(reverseMappingCollection.type());
                reverseMappingCollection.set(dto, currentCollection);
            }

            compositeUpdateResult.results().forEach(updateResult -> {
                if (updateResult.getDto().getClass() == reverseMappingCollection.genericType()) {
                    // Matching collection class - add the updated value to the collection if necessary
                    //TODO: support for multiple collections of the same type in the parent DTO
                    if (!currentCollection.contains(updateResult.getDto())) {
                        LOGGER.trace("Adding DTO to reverse mapping collection '{}': {}", mappedOneToMany.collection().name(), updateResult.getDto());
                        currentCollection.add(updateResult.getDto());
                    }
                }
            });
        });
    }

//    private AbstractStatementBuilder<?> createStatementBuilder(final Object dto) {
//        final OrmTable table = tableProvider.getTableOrThrow(dto.getClass());
//
//        if (table.isPersistedDto(dto)) {
//            return createUpdateBuilder(dto, table);
//        } else {
//            return createInsertBuilder(dto, table);
//        }
//    }

    private AbstractStatementBuilder<?> createStatementBuilder(final Object dto, final Set<Object> inProgressDtos) {
        if (inProgressDtos.contains(dto)) {
            LOGGER.trace("Skipping DTO: {} - already in progress", dto);
            return null;
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
    private CompositeUpdateResult executeUpdateStatement(final Object dto, final @Nullable DtoUpdateResult parentResult, final StatementBuilder<?> statementBuilder) throws SQLException {
        final CompositeUpdateResult result = new CompositeUpdateResult();
        final DtoUpdateResult dtoUpdateResult = new DtoUpdateResult(dto, parentResult);

        for (Map.Entry<Object, PipedStatement> entry : statementBuilder.statementChain().getDependencies().entrySet()) {
            final PipedStatement pipedStatement = entry.getValue();

            if (pipedStatement.statementBuilder() instanceof NoOpStatementBuilder) {
                LOGGER.trace("Resolving piped statement directly: {}", pipedStatement);
                pipedStatement.valuePipe().accept(ObjectUtils.requireNonNull(parentResult, () -> new IllegalStateException("NoOpStatementBuilder without parent result")).getUpdateResult());
                continue;
            }

            final CompositeUpdateResult dependencyResult = executeUpdateStatement(pipedStatement.dto(), dtoUpdateResult, pipedStatement.statementBuilder());
            result.merge(dependencyResult);
            pipedStatement.valuePipe().accept(dependencyResult.primary().getUpdateResult());
        }

        UpdateStatement updateStatement;

        try {
            updateStatement = statementBuilder.build();
        } catch (final IllegalArgumentException ex) {
            // No columns to update
            updateStatement = null;
        }

        if (updateStatement instanceof Insert insert) {
            dtoUpdateResult.setUpdateResult(databaseProvider.insert(insert));
        } else if (updateStatement instanceof Update update) {
            dtoUpdateResult.setUpdateResult(databaseProvider.update(update));
        } else if (statementBuilder instanceof InsertBuilder) {
            dtoUpdateResult.setUpdateResult(new InsertResult(0));
        } else {
            dtoUpdateResult.setUpdateResult(new UpdateResult(0));
        }

        result.add(dtoUpdateResult);

        for (Map.Entry<Object, PipedStatement> entry : statementBuilder.statementChain().getDependants().entrySet()) {
            final PipedStatement pipedStatement = entry.getValue();
            pipedStatement.valuePipe().accept(dtoUpdateResult.getUpdateResult());
            final CompositeUpdateResult dependantResult = executeUpdateStatement(pipedStatement.dto(), dtoUpdateResult, pipedStatement.statementBuilder());
            result.merge(dependantResult);
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
                    table = tableRegistry.getTable(dtoClass);
                } else {
                    // Root table registry - if not found, throw an exception
                    table = tableRegistry.getTableOrThrow(dtoClass);
                }

                if (table != null) {
                    return table;
                }
            }

            throw new IllegalArgumentException("No table found for DTO class: " + dtoClass);
        }
    }
}
