package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.ClassUtils;
import org.litebridge.commons.CollectionUtils;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.query.Condition;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.db.spi.update.ColumnValue;
import org.litebridge.db.spi.update.Insert;
import org.litebridge.db.spi.update.InsertResult;
import org.litebridge.db.spi.update.RowValue;
import org.litebridge.db.spi.update.Update;
import org.litebridge.db.spi.update.UpdateResult;
import org.litebridge.db.spi.update.UpdateStatement;
import org.litebridge.tracking.ChangedField;
import org.litebridge.tracking.ChangedFields;
import org.litebridge.tracking.FieldAccessor;
import org.litebridge.tracking.TrackedDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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
    private final TableRegistry tableRegistry;
    private final DatabaseProvider databaseProvider;

    public PersistenceFacade(final TableRegistry tableRegistry,
                             final DatabaseProvider databaseProvider) {
        this.tableRegistry = tableRegistry;
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
        final AbstractStatementBuilder<?> statementBuilder = createStatementBuilder(dto);
        final UpdateResult updateResult = executeUpdateStatement(statementBuilder);

        if (updateResult instanceof InsertResult insertResult
                && !CollectionUtils.isEmpty(insertResult.generatedKeys())) {
            // TODO: composite PK support
            updateDtoPrimaryKey(dto, insertResult.generatedKeys().getFirst());
        }
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
        final InsertBuilder statementBuilder = createInsertBuilder(dto, tableRegistry.getTableOrThrow(dto.getClass()));
        final InsertResult insertResult = (InsertResult) executeUpdateStatement(statementBuilder);

        if (!CollectionUtils.isEmpty(insertResult.generatedKeys())) {
            // TODO: composite PK support
            updateDtoPrimaryKey(dto, insertResult.generatedKeys().getFirst());
        }
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
        final UpdateBuilder statementBuilder = createUpdateBuilder(dto, tableRegistry.getTableOrThrow(dto.getClass()));
        executeUpdateStatement(statementBuilder);
    }

    private InsertBuilder createInsertBuilder(final Object dto, final OrmTable table) {
        final InsertBuilder insertBuilder = new InsertBuilder(table);
        prepareUpdateStatement(dto, table, insertBuilder);
        return insertBuilder;
    }

    private UpdateBuilder createUpdateBuilder(final Object dto, final OrmTable table) {
        final UpdateBuilder updateBuilder = new UpdateBuilder(table);
        prepareUpdateStatement(dto, table, updateBuilder);
        return updateBuilder;
    }

    private <DTO> @Nullable StatementChain prepareUpdateStatement(final DTO dto, final OrmTable table, final AbstractStatementBuilder<?> statementBuilder) {
        final TrackedDto<?> trackedDto = table.ensureTrackedDto(dto);
        final ChangedFields changedFields = trackedDto.changedFields();

        if (changedFields.isEmpty()) {
            LOGGER.debug("No changed fields found for DTO: {}", dto);
            return null;
        }

        if (LOGGER.isTraceEnabled()) {
            final StringJoiner sj = new StringJoiner(",", "[", "]");
            changedFields.forEach(changedField -> sj.add(changedField.name() + " = " + changedField.value()));
            LOGGER.trace("Changed fields for DTO: {}: {}", dto, sj);
        }

        final StatementChain statementChain = statementBuilder.statementChain();
        final List<ColumnValue> columnValues = new ArrayList<>();
        final boolean isInsert = statementBuilder instanceof InsertBuilder;

        for (ColumnMetaData column : table.getMetaData().columns()) {
            final String fieldName = table.getFieldForColumnName(column.name()).name();
            final ChangedField changedField = changedFields.getOrNull(fieldName);
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
                // Dealing with an embedded DTO
                final OrmTable embeddedDtoTable = tableRegistry.getTableOrThrow(value.getClass());

                if (!embeddedDtoTable.isPersistedDto(value)) {
                    // Cascade save to the embedded DTO
                    final PipedStatement existingStatement = statementChain.getDependency(value);

                    if (existingStatement == null) {
                        final AbstractStatementBuilder<?> dependencyStatementBuilder = createStatementBuilder(value);

                        final PipedStatement dependencyPipe = new PipedStatement(dependencyStatementBuilder, updateResult -> {
                            if (updateResult instanceof InsertResult insertResult
                                    && !CollectionUtils.isEmpty(insertResult.generatedKeys())) {
                                // TODO: composite PK support
                                final Object pkValue = insertResult.generatedKeys().getFirst();
                                columnValues.add(new ColumnValue(column, pkValue));
                                updateDtoPrimaryKey(value, pkValue);
                            }
                        });

                        statementChain.addDependency(value, dependencyPipe);
                    }
                } else {
                    // Get the primary key
                    final List<ColumnMetaData> embeddedDtoPk = embeddedDtoTable.getMetaData().primaryKey();
                    // TODO: composite PK support
                    final FieldAccessor field = embeddedDtoTable.getFieldForColumnName(embeddedDtoPk.get(0).name());
                    final Object pkValue = field.get(value);
                    columnValues.add(new ColumnValue(column, pkValue));
                }
            }
        }

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

    private void updateDtoPrimaryKey(final Object dto, final Object generatedKey) {
        final OrmTable embeddedDtoTable = tableRegistry.getTableOrThrow(dto.getClass());
        final List<ColumnMetaData> embeddedDtoPk = embeddedDtoTable.getMetaData().primaryKey();
        // TODO: composite PK support
        final ColumnMetaData pkColumn = embeddedDtoPk.get(0);
        final FieldAccessor field = embeddedDtoTable.getFieldForColumnName(pkColumn.name());
        final Object convertedValue = databaseProvider.getTypeConverter().convert(generatedKey, pkColumn.getDataType());
        field.set(dto, convertedValue);
        embeddedDtoTable.syncPersistedDto(dto);
    }

    private AbstractStatementBuilder<?> createStatementBuilder(final Object dto) {
        final OrmTable table = tableRegistry.getTableOrThrow(dto.getClass());

        if (table.isPersistedDto(dto)) {
            return createUpdateBuilder(dto, table);
        } else {
            return createInsertBuilder(dto, table);
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
    private UpdateResult executeUpdateStatement(final AbstractStatementBuilder<?> statementBuilder) throws SQLException {
        for (Map.Entry<Object, PipedStatement> entry : statementBuilder.statementChain().getDependencies().entrySet()) {
            final PipedStatement pipedStatement = entry.getValue();
            final UpdateResult dependencyResult = executeUpdateStatement(pipedStatement.statementBuilder());
            pipedStatement.valuePipe().accept(dependencyResult);
        }

        final UpdateStatement updateStatement;

        try {
            updateStatement = statementBuilder.build();
        } catch (final IllegalArgumentException ex) {
            // No columns to update
            if (statementBuilder instanceof InsertBuilder) {
                return new InsertResult(0);
            } else {
                return new UpdateResult(0);
            }
        }

        if (updateStatement instanceof Insert insert) {
            return databaseProvider.insert(insert);
        } else {
            final Update update = (Update) updateStatement;
            return databaseProvider.update(update);
        }
    }
}
