package org.litebridge.orm.engine.compiler;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.query.ConditionGroup;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.db.spi.update.Merge;
import org.litebridge.db.spi.update.UpdateColumn;
import org.litebridge.orm.api.select.ast.InsertNode;
import org.litebridge.orm.api.select.ast.MergeNode;
import org.litebridge.orm.api.select.ast.SetNode;
import org.litebridge.orm.api.select.ast.UsingNode;
import org.litebridge.orm.api.select.model.SelectExpressionMapper;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ColumnExpressionSpec;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.meta.QueryField;
import org.litebridge.orm.meta.QueryFieldInspector;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.TableMetaDataCache;
import org.litebridge.orm.persistence.TableRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

final class MergeCompilationContext implements CompilationContext {

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
    private final List<BindValue> bindValues = new ArrayList<>();
    private @Nullable UsingNode usingNode;
    private MergeCompilationContext.ConditionContext conditionContext;

    MergeCompilationContext(final MergeNode mergeNode,
                            final LitebridgeContext litebridgeContext) {
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

    public void setUsingNode(UsingNode usingNode) {
        this.usingNode = usingNode;
        this.conditionContext = ConditionContext.ON;
    }

    public WhenMatchedSpec getWhenMatchedSpec() {
        return whenMatchedSpecs.getLast();
    }

    public WhenMatchedSpec addWhenMatchedSpec(final boolean matched) {
        if (matched) {
            this.conditionContext = ConditionContext.WHEN_MATCHED;
        } else {
            this.conditionContext = ConditionContext.WHEN_NOT_MATCHED;
        }

        final WhenMatchedSpec whenMatchedSpec = new WhenMatchedSpec(matched);
        whenMatchedSpecs.add(whenMatchedSpec);
        return whenMatchedSpec;
    }

    public void whenMatchedUpdateSet(final SetNode setNode) {
        final WhenMatchedSpec whenMatchedSpec = whenMatchedSpecs.getLast();
        final ColumnMetaData columnMetaData;

        if (setNode.column() != null) {
            columnMetaData = targetTableMetaData.column(setNode.column());
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

        if (setNode.bindValue()) {
            bindValues.add(new BindValue(setNode.value(), columnMetaData.getDataType()));
        } else {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        //TODO: implement generated values
    }

    public void whenNotMatchedInsert(final InsertNode insertNode) {
        final WhenMatchedSpec whenMatchedSpec = whenMatchedSpecs.getLast();
        final String[] columnNames = insertNode.columns();
        final ExpressionSpec[] expressionSpecs = insertNode.expressionSpecs();
        final List<ColumnMetaData> columnMetaDataList;

        if (columnNames != null) {
            columnMetaDataList = new ArrayList<>(columnNames.length);

            for (String columnName : columnNames) {
                columnMetaDataList.add(targetTableMetaData.column(columnName));
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
            throw new IllegalArgumentException("No columns or expressions specified");
        }

        whenMatchedSpec.addUpdateColumns(columnMetaDataList);
    }

    @Override
    public List<BindValue> getBindValues() {
        return bindValues;
    }

    public void addBindValues(final @Nullable Object... values) {
        switch (conditionContext) {
            case WHEN_MATCHED, WHEN_NOT_MATCHED -> {
                final List<ColumnMetaData> columnMetaDataList = getWhenMatchedSpec().getColumnMetaDataList();

                for (int i = 0; i < values.length; i++) {
                    //TODO: fix datatype detection
                    final int sqlDataType = columnMetaDataList != null ? columnMetaDataList.get(i).getDataType() : 0;
                    bindValues.add(new BindValue(values[i], sqlDataType));
                }
            }
            case ON -> {
                for (int i = 0; i < values.length; i++) {
                    //TODO: fix datatype detection
                    final int sqlDataType = 0;
                    bindValues.add(new BindValue(values[i], sqlDataType));
                }
            }
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

        final List<BindValue> bindValues = new ArrayList<>();

        final List<Merge.WhenMatched<Merge.WhenMatchedOperation>> whenMatchedList = new ArrayList<>();
        final List<Merge.WhenMatched<Merge.MergeInsert>> whenNotMatchedList = new ArrayList<>();

        for (WhenMatchedSpec whenMatchedSpec : whenMatchedSpecs) {
            final ConditionGroupSpecStack andConditionGroupStack = whenMatchedSpec.getAndConditionGroupStack();
            final ConditionGroup andConditionGroup;

            if (andConditionGroupStack != null) {
                andConditionGroup = andConditionGroupStack.current().toConditionGroup(selectExpressionMapper,
                        Set.of(targetTable, usingTable),
                        bindValues,
                        tableMetaDataCache,
                        typeConverter);
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
        }

        return new Merge(targetTable,
                usingTable,
                null,
                on.current().toConditionGroup(selectExpressionMapper,
                        Set.of(targetTable, usingTable),
                        bindValues,
                        tableMetaDataCache,
                        typeConverter),
                whenMatchedList,
                whenNotMatchedList);
    }


    public ConditionGroupSpecStack getConditionGroupSpecStack() {
        return switch (conditionContext) {
            case ON -> on;
            case WHEN_MATCHED, WHEN_NOT_MATCHED -> whenMatchedSpecs.getLast().ensureAndConditionGroupStack();
        };
    }

    static final class WhenMatchedSpec {
        private final boolean matched;
        private @Nullable ConditionGroupSpecStack and;
        private @Nullable List<ColumnMetaData> columnMetaDataList;
        private @Nullable List<UpdateColumn> updateColumns;
        private boolean delete;

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
                    .map(columnMetaData -> new UpdateColumn(columnMetaData.name(), null))
                    .toList();
            ensureUpdateColumns().addAll(updateColumns);
        }

        public void addUpdateColumn(final ColumnMetaData column) {
            ensureColumnMetaDataList().add(column);
            ensureUpdateColumns().add(new UpdateColumn(column.name(), null));
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

    enum ConditionContext {
        ON,
        WHEN_MATCHED,
        WHEN_NOT_MATCHED
    }
}
