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

public class PersistenceFacade {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceFacade.class);
    private final TableRegistry tableRegistry;
    private final DatabaseProvider databaseProvider;

    public PersistenceFacade(final TableRegistry tableRegistry,
                             final DatabaseProvider databaseProvider) {
        this.tableRegistry = tableRegistry;
        this.databaseProvider = databaseProvider;
    }

    public <DTO> void save(final Collection<DTO> dtos) throws SQLException {
        for (DTO dto : dtos) {
            save(dto);
        }
    }

    public <DTO> void save(DTO dto) throws SQLException {
        final AbstractStatementBuilder<?> statementBuilder = createStatementBuilder(dto);
        final UpdateResult updateResult = executeUpdateStatement(statementBuilder);

        if (updateResult instanceof InsertResult insertResult
                && !CollectionUtils.isEmpty(insertResult.generatedKeys())) {
            // TODO: composite PK support
            updateDtoPrimaryKey(dto, insertResult.generatedKeys().getFirst());
        }
    }

    private InsertBuilder createInsertBuilder(final Object dto, final Table table) {
        final InsertBuilder insertBuilder = new InsertBuilder(table);
        prepareUpdateStatement(dto, table, insertBuilder);
        return insertBuilder;
    }

    private UpdateBuilder createUpdateBuilder(final Object dto, final Table table) {
        final UpdateBuilder updateBuilder = new UpdateBuilder(table);
        prepareUpdateStatement(dto, table, updateBuilder);
        return updateBuilder;
    }

    private <DTO> @Nullable StatementChain prepareUpdateStatement(final DTO dto, final Table table, final AbstractStatementBuilder<?> statementBuilder) {
        final TrackedDto<?> trackedDto = table.getTrackedDto(dto);
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
                final Table embeddedDtoTable = tableRegistry.getTableOrThrow(value.getClass());

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
                    final List<String> embeddedDtoPk = embeddedDtoTable.getMetaData().primaryKey();
                    // TODO: composite PK support
                    final FieldAccessor field = embeddedDtoTable.getFieldForColumnName(embeddedDtoPk.get(0));
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
                final ColumnMetaData column = table.getColumn(pkColumn);
                final FieldAccessor field = table.getFieldForColumnName(column.name());
                final Object pkValue = field.get(dto);
                final Condition condition;

                if (pkValue != null) {
                    condition = new Condition(column, Operator.EQ, pkValue);
                } else {
                    condition = new Condition(column, Operator.IS_NULL);
                }

                updateBuilder.where(condition);
            });
        }

        return statementChain;
    }

    private void updateDtoPrimaryKey(final Object dto, final Object generatedKey) {
        final Table embeddedDtoTable = tableRegistry.getTableOrThrow(dto.getClass());
        final List<String> embeddedDtoPk = embeddedDtoTable.getMetaData().primaryKey();
        // TODO: composite PK support
        final FieldAccessor field = embeddedDtoTable.getFieldForColumnName(embeddedDtoPk.get(0));
        field.set(dto, generatedKey);
        embeddedDtoTable.syncPersistedDto(dto);
    }

    private AbstractStatementBuilder<?> createStatementBuilder(final Object dto) {
        final Table table = tableRegistry.getTableOrThrow(dto.getClass());

        if (table.isPersistedDto(dto)) {
            return createUpdateBuilder(dto, table);
        } else {
            return createInsertBuilder(dto, table);
        }
    }

    private UpdateResult executeUpdateStatement(final AbstractStatementBuilder<?> statementBuilder) throws SQLException {
        for (Map.Entry<Object, PipedStatement> entry : statementBuilder.statementChain().getDependencies().entrySet()) {
            final PipedStatement pipedStatement = entry.getValue();
            final UpdateResult dependencyResult = executeUpdateStatement(pipedStatement.statementBuilder());
            pipedStatement.valuePipe().accept(dependencyResult);
        }

        final UpdateStatement updateStatement = statementBuilder.build();

        if (updateStatement instanceof Insert insert) {
            return databaseProvider.insert(insert);
        } else {
            final Update update = (Update) updateStatement;
            return databaseProvider.update(update);
        }
    }
}
