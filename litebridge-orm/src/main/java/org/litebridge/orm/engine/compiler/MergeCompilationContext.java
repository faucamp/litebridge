package org.litebridge.orm.engine.compiler;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.ClassUtils;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.ForeignKeyConstraint;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.query.ConditionGroup;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.db.spi.update.Merge;
import org.litebridge.db.spi.update.UpdateColumn;
import org.litebridge.orm.api.select.model.ConditionGroupSpec;
import org.litebridge.orm.api.select.model.SelectExpressionMapper;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.ConditionNode;
import org.litebridge.orm.engine.ast.InsertDtoValuesNode;
import org.litebridge.orm.engine.ast.InsertNode;
import org.litebridge.orm.engine.ast.InsertValuesNode;
import org.litebridge.orm.engine.ast.MergeNode;
import org.litebridge.orm.engine.ast.SetNode;
import org.litebridge.orm.engine.ast.UsingNode;
import org.litebridge.orm.expression.ColumnExpressionSpec;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.meta.QueryField;
import org.litebridge.orm.meta.QueryFieldInspector;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.TableMetaDataCache;
import org.litebridge.orm.persistence.TableRegistry;
import org.litebridge.tracking.FieldAccessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Compilation context for MERGE INTO statements.
 */
final class MergeCompilationContext extends AbstractCompilationContext {

    private final MergeNode mergeNode;
    private final TableMetaData targetTableMetaData;
    private final @Nullable OrmTable targetOrmTable;
    private final Table targetTable;
    private final SelectExpressionMapper selectExpressionMapper;
    private final TableMetaDataCache tableMetaDataCache;
    private final TableRegistry tableRegistry;
    private final TypeConverter typeConverter;
    private final ConditionGroupSpecStack on = new ConditionGroupSpecStack();
    private final List<WhenMatchedSpec> whenMatchedSpecs = new ArrayList<>();
    private @Nullable UsingNode usingNode;
    private @Nullable ConditionContext conditionContext;

    MergeCompilationContext(final MergeNode mergeNode,
                            final LitebridgeContext litebridgeContext) {
        super(litebridgeContext);
        this.mergeNode = mergeNode;
        this.selectExpressionMapper = litebridgeContext.selectExpressionMapper();
        this.tableRegistry = litebridgeContext.tableRegistry();
        this.tableMetaDataCache = litebridgeContext.tableMetaDataCache();
        this.typeConverter = litebridgeContext.typeConverter();

        if (mergeNode.table() != null) {
            this.targetOrmTable = tableRegistry.getOrmTable(mergeNode.table());
        } else {
            this.targetOrmTable = tableRegistry.getOrmTable(Objects.requireNonNull(mergeNode.dtoClass()));
        }

        if (targetOrmTable != null) {
            this.targetTableMetaData = targetOrmTable.getMetaData();
        } else {
            this.targetTableMetaData = this.tableMetaDataCache.ensureTableMetaData(tableRegistry.getOrCreateSpiTable(mergeNode.table()));
        }

        this.targetTable = targetTableMetaData.toTable();
    }

    /**
     * Sets the USING clause information.
     *
     * @param usingNode The USING node to apply.
     */
    public void setUsingNode(final UsingNode usingNode) {
        this.usingNode = usingNode;
        this.conditionContext = ConditionContext.ON;
    }

    public ConditionContext conditionContext() {
        return Objects.requireNonNull(conditionContext, "Condition context not set");
    }

    /**
     * Adds a condition to the ON clause for USING.
     *
     * @param conditionNode The condition node to apply.
     */
    public void addOnCondition(final ConditionNode conditionNode) {
        addConditionToGroup(conditionNode, on.current());
    }

    public ConditionGroupSpecStack onConditionGroupStack() {
        return on;
    }

    public WhenMatchedSpec getWhenMatchedSpec() {
        return whenMatchedSpecs.getLast();
    }

    public void addWhenMatchedSpec(final boolean matched) {
        this.conditionContext = matched ? ConditionContext.WHEN_MATCHED : ConditionContext.WHEN_NOT_MATCHED;
        final WhenMatchedSpec whenMatchedSpec = new WhenMatchedSpec(matched);
        whenMatchedSpecs.add(whenMatchedSpec);
    }

    /**
     * Adds an AND condition to the current WHEN MATCHED/WHEN NOT MATCHED clause.
     *
     * @param conditionNode The condition node to apply.
     */
    public void addMatchAndCondition(final ConditionNode conditionNode) {
        final ConditionGroupSpec conditionGroupSpec = matchAndConditionGroupStack().current();
        addConditionToGroup(conditionNode, conditionGroupSpec);
    }

    public ConditionGroupSpecStack matchAndConditionGroupStack() {
        return whenMatchedSpecs.getLast().ensureAndConditionGroupStack();
    }

    public void whenMatchedUpdateSet(final SetNode setNode) {
        final WhenMatchedSpec whenMatchedSpec = whenMatchedSpecs.getLast();
        final ColumnMetaData columnMetaData;

        if (setNode.column() != null) {
            if (litebridgeContext.mode() == LitebridgeContext.Mode.DTO && targetOrmTable != null) {
                columnMetaData = targetOrmTable.columnMetaDataForField(setNode.column());
            } else {
                columnMetaData = targetTableMetaData.column(setNode.column());
            }
        } else {
            final ExpressionSpec expressionSpec = setNode.expressionSpec();

            if (expressionSpec instanceof QueryField queryField) {
                columnMetaData = targetOrmTable.columnMetaDataForField(QueryFieldInspector.getFieldName(queryField));
            } else if (expressionSpec instanceof ColumnExpressionSpec columnExpressionSpec) {
                columnMetaData = targetTableMetaData.column(columnExpressionSpec.getColumn().name());
            } else {
                throw new IllegalArgumentException("Unsupported expression spec type: " + expressionSpec.getClass().getName());
            }
        }

        whenMatchedSpec.addUpdateColumn(columnMetaData);

        if (setNode.mathOperator() != null) {
            //TODO: implement generated values
            throw new UnsupportedOperationException("Not yet implemented");
        } else {
            whenMatchedSpec.addBindValue(new BindValue(setNode.value(), columnMetaData.getDataType()));
        }
    }

    public void whenNotMatchedInsert(final InsertNode insertNode) {
        final WhenMatchedSpec whenMatchedSpec = whenMatchedSpecs.getLast();
        final String[] columnNames = insertNode.columns();
        final ExpressionSpec[] expressionSpecs = insertNode.expressionSpecs();
        final List<ColumnMetaData> columnMetaDataList;

        if (columnNames != null) {
            columnMetaDataList = new ArrayList<>(columnNames.length);

            for (String columnName : columnNames) {
                if (litebridgeContext.mode() == LitebridgeContext.Mode.DTO && targetOrmTable != null) {
                    columnMetaDataList.add(targetOrmTable.columnMetaDataForField(columnName));
                } else {
                    columnMetaDataList.add(targetTableMetaData.column(columnName));
                }
            }
        } else if (expressionSpecs != null) {
            columnMetaDataList = new ArrayList<>(expressionSpecs.length);

            for (ExpressionSpec expressionSpec : expressionSpecs) {
                if (expressionSpec instanceof ColumnExpressionSpec columnExpressionSpec) {
                    columnMetaDataList.add(targetTableMetaData.column(columnExpressionSpec.getColumn().name()));
                } else if (expressionSpec instanceof QueryField queryField) {
                    final Class<?> dtoClass = QueryFieldInspector.getDtoClass(queryField);
                    final String fieldName = QueryFieldInspector.getFieldName(queryField);
                    final ColumnMetaData columnMetaData = targetOrmTable.columnMetaDataForField(fieldName);

                    if (columnMetaData == null) {
                        throw new IllegalArgumentException("No column found for field: " + fieldName);
                    }

                    columnMetaDataList.add(columnMetaData);
                } else {
                    throw new UnsupportedOperationException("Unsupported expression spec: " + expressionSpec);
                }
            }
        } else {
            // Update columns will be supplied via an InsertDtoValueNode
            return;
        }

        whenMatchedSpec.addUpdateColumns(columnMetaDataList);
    }

    public void addInsertValues(final InsertValuesNode insertValuesNode) {
        final WhenMatchedSpec whenMatchedSpec = getWhenMatchedSpec();
        final List<ColumnMetaData> columnMetaDataList = getWhenMatchedSpec().getColumnMetaDataList();
        final Object[] values = insertValuesNode.values();

        for (int i = 0; i < values.length; i++) {
            //TODO: fix datatype detection
            final int sqlDataType = columnMetaDataList != null ? columnMetaDataList.get(i).getDataType() : 0;
            whenMatchedSpec.addBindValue(new BindValue(values[i], sqlDataType));
        }
    }

    public void addInsertDtoValues(final InsertDtoValuesNode insertDtoValuesNode) {
        final OrmTable targetOrmTable = Objects.requireNonNull(this.targetOrmTable);
        final List<ColumnMetaData> columnMetaDataList = targetOrmTable.mappedColumns();
        final WhenMatchedSpec whenMatchedSpec = getWhenMatchedSpec();
        final Object dto = insertDtoValuesNode.dto();

        for (ColumnMetaData columnMetaData : columnMetaDataList) {
            final FieldAccessor fieldAccessor = targetOrmTable.fieldForColumnNameOrNull(columnMetaData.name());

            if (fieldAccessor == null) {
                continue;
            }

            final Object value = fieldAccessor.get(dto);

            if (value != null) {
                final List<ForeignKeyConstraint> foreignKeyConstraints = columnMetaData.getForeignKeyConstraints();

                if (!foreignKeyConstraints.isEmpty() && !ClassUtils.isBasicType(fieldAccessor.type())) {
                    final OrmTable fkOrmTable = litebridgeContext.tableRegistry().getOrmTableOrThrow(value.getClass());

                    for (final ForeignKeyConstraint fkc : foreignKeyConstraints) {
                        final FieldAccessor fkFieldAccessor = fkOrmTable.getFieldForColumnName(fkc.foreignKey().name());
                        final Object pkValue = fkFieldAccessor.get(value);
                        whenMatchedSpec.addUpdateColumn(columnMetaData);
                        whenMatchedSpec.addBindValue(new BindValue(pkValue, columnMetaData.getDataType()));
                        return;
                    }
                }
            } else {
                if (!columnMetaData.isNullable()
                        && (columnMetaData.isAutoIncrement() || columnMetaData.getGenerator() != null)) {
                    // Just add the insert column definition, not a bind value (generator will be used)
                    whenMatchedSpec.addUpdateColumn(columnMetaData);
                    continue;
                }

                throw new IllegalArgumentException("Column " + columnMetaData.name() + " is not nullable and has no generator");
            }

            whenMatchedSpec.addUpdateColumn(columnMetaData);
            whenMatchedSpec.addBindValue(new BindValue(value, columnMetaData.getDataType()));
        }
    }

    @Override
    public Operation toOperation() {
        final UsingNode usingNode = Objects.requireNonNull(this.usingNode);
        final Table usingTable;

        if (usingNode.table() != null) {
            usingTable = tableRegistry.getOrCreateSpiTable(usingNode.table());
        } else {
            usingTable = Objects.requireNonNull(tableRegistry
                            .getOrmTable(Objects.requireNonNull(usingNode.dtoClass())))
                    .getMetaData().toTable();
        }

        final List<Merge.WhenMatched<Merge.WhenMatchedOperation>> whenMatchedList = new ArrayList<>();
        final List<Merge.WhenMatched<Merge.MergeInsert>> whenNotMatchedList = new ArrayList<>();

        for (WhenMatchedSpec whenMatchedSpec : whenMatchedSpecs) {
            final ConditionGroupSpecStack andConditionGroupStack = whenMatchedSpec.getAndConditionGroupStack();
            final ConditionGroup andConditionGroup;

            if (andConditionGroupStack != null) {
                andConditionGroup = toConditionGroup(andConditionGroupStack.current(), null, targetTable);
            } else {
                andConditionGroup = null;
            }

            if (whenMatchedSpec.isMatched()) {
                // When matched
                final Merge.WhenMatchedOperation operation;

                if (whenMatchedSpec.isDelete()) {
                    operation = new Merge.MergeDelete();
                } else {
                    operation = new Merge.MergeUpdate(whenMatchedSpec.getUpdateColumns());
                }

                whenMatchedList.add(new Merge.WhenMatched<>(andConditionGroup, operation));
            } else {
                // When not matched
                final Merge.WhenMatched<Merge.MergeInsert> whenNotMatched = new Merge.WhenMatched<>(null, new Merge.MergeInsert(whenMatchedSpec.getUpdateColumns(), 1));
                whenNotMatchedList.add(whenNotMatched);
            }

            bindValues.addAll(whenMatchedSpec.getBindValues());
        }

        final ConditionGroup onConditionGroup = toConditionGroup(on.current(), null, usingTable);

        return new Merge(targetTable,
                usingTable,
                null,
                onConditionGroup,
                whenMatchedList,
                whenNotMatchedList);
    }

    static final class WhenMatchedSpec {
        private final boolean matched;
        private @Nullable ConditionGroupSpecStack and;
        private @Nullable List<ColumnMetaData> columnMetaDataList;
        private @Nullable List<UpdateColumn> updateColumns;
        private boolean delete;
        private @Nullable List<String> bindValueUpdateColumnNames;
        private @Nullable List<BindValue> bindValues;

        WhenMatchedSpec(final boolean matched) {
            this.matched = matched;
        }

        public boolean isMatched() {
            return matched;
        }

        public ConditionGroupSpecStack ensureAndConditionGroupStack() {
            if (and == null) {
                and = new ConditionGroupSpecStack();
            }

            return and;
        }

        public @Nullable ConditionGroupSpecStack getAndConditionGroupStack() {
            return and;
        }

        public void addUpdateColumns(final List<ColumnMetaData> columnMetaDataList) {
            ensureColumnMetaDataList().addAll(columnMetaDataList);
            final List<UpdateColumn> updateColumns = columnMetaDataList.stream()
                    .map(columnMetaData -> new UpdateColumn(columnMetaData.name()))
                    .toList();
            ensureUpdateColumns().addAll(updateColumns);
        }

        public void addUpdateColumn(final ColumnMetaData columnMetaData) {
            ensureColumnMetaDataList().add(columnMetaData);
            ensureUpdateColumns().add(new UpdateColumn(columnMetaData.name(), columnMetaData.getGenerator(), null));
        }

        public @Nullable List<UpdateColumn> getUpdateColumns() {
            return updateColumns;
        }

        public boolean isDelete() {
            return delete;
        }

        public void setDelete(final boolean delete) {
            this.delete = delete;
        }

        public @Nullable List<ColumnMetaData> getColumnMetaDataList() {
            return columnMetaDataList;
        }

        public List<BindValue> getBindValues() {
            return bindValues != null ? bindValues : Collections.emptyList();
        }

        public void addBindValue(final BindValue bindValue) {
            if (bindValues == null) {
                bindValues = new ArrayList<>();
            }

            bindValues.add(bindValue);
        }

        public void addUpdateColumn(final UpdateColumn updateColumn) {
            ensureUpdateColumns().add(updateColumn);
        }

        private List<UpdateColumn> ensureUpdateColumns() {
            if (updateColumns == null) {
                updateColumns = new ArrayList<>();
            }

            return updateColumns;
        }

        private List<ColumnMetaData> ensureColumnMetaDataList() {
            if (columnMetaDataList == null) {
                columnMetaDataList = new ArrayList<>();
            }

            return columnMetaDataList;
        }
    }

    private static void addConditionToGroup(final ConditionNode conditionNode, final ConditionGroupSpec conditionGroupSpec) {
        conditionGroupSpec.newCondition(conditionNode.logicOperator(),
                conditionNode.lhsColumn(),
                conditionNode.lhsExpression(),
                conditionNode.operator(),
                conditionNode.rhs());
    }

    enum ConditionContext {
        ON,
        WHEN_MATCHED,
        WHEN_NOT_MATCHED
    }
}
