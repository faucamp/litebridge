package org.litebridge.orm.engine;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.query.ConditionGroup;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.db.spi.update.InsertV2;
import org.litebridge.db.spi.update.Merge;
import org.litebridge.orm.api.select.ast.InsertNode;
import org.litebridge.orm.api.select.ast.MergeNode;
import org.litebridge.orm.api.select.ast.SetNode;
import org.litebridge.orm.api.select.ast.UsingNode;
import org.litebridge.orm.api.select.model.SelectExpressionMapper;
import org.litebridge.orm.persistence.TableMetaDataCache;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

final class MergeCompilationContext implements CompilationContext {

    private final MergeNode mergeNode;
    private final TableMetaData targetTableMetaData;
    private final SelectExpressionMapper selectExpressionMapper;
    private final TableMetaDataCache tableMetaDataCache;
    private final TypeConverter typeConverter;
    private final ConditionGroupSpecStack on = new ConditionGroupSpecStack();
    private final List<WhenMatchedSpec> whenMatchedSpecs = new ArrayList<>();
    private final List<BindValue> bindValues = new ArrayList<>();
    private @Nullable UsingNode usingNode;
    private MergeCompilationContext.ConditionContext conditionContext;

    public MergeCompilationContext(final MergeNode mergeNode,
                                   final SelectExpressionMapper selectExpressionMapper,
                                   final TableMetaDataCache tableMetaDataCache,
                                   final TypeConverter typeConverter) {
        this.mergeNode = mergeNode;
        this.selectExpressionMapper = selectExpressionMapper;
        this.tableMetaDataCache = tableMetaDataCache;
        this.typeConverter = typeConverter;
        this.targetTableMetaData = this.tableMetaDataCache.ensureTableMetaData(Objects.requireNonNull(mergeNode.table()));
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
        final ColumnMetaData columnMetaData = targetTableMetaData.column(setNode.column().name());
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
        final List<ColumnMetaData> columnMetaDataList = new ArrayList<>(columnNames.length);

        for (String columnName : insertNode.columns()) {
            columnMetaDataList.add(targetTableMetaData.column(columnName));
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
        final Table usingTable = usingNode.table();
        final List<BindValue> bindValues = new ArrayList<>();

        final List<Merge.WhenMatched<Merge.WhenMatchedOperation>> whenMatchedList = new ArrayList<>();
        final List<Merge.WhenMatched<Merge.MergeInsert>> whenNotMatchedList = new ArrayList<>();

        for (WhenMatchedSpec whenMatchedSpec : whenMatchedSpecs) {
            final ConditionGroupSpecStack andConditionGroupStack = whenMatchedSpec.getAndConditionGroupStack();
            final ConditionGroup andConditionGroup;

            if (andConditionGroupStack != null) {
                andConditionGroup = andConditionGroupStack.current().toConditionGroup(selectExpressionMapper,
                        Set.of(mergeNode.table(), usingTable),
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

        return new Merge(mergeNode.table(),
                usingTable,
                null,
                on.current().toConditionGroup(selectExpressionMapper,
                        Set.of(mergeNode.table(), usingTable),
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
            default -> throw new UnsupportedOperationException("Not yet implemented");
        };
    }

    static final class WhenMatchedSpec {
        private final boolean matched;
        private @Nullable ConditionGroupSpecStack and;
        private @Nullable List<ColumnMetaData> columnMetaDataList;
        private @Nullable List<InsertV2.InsertColumn> updateColumns;
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
            final List<InsertV2.InsertColumn> updateColumns = columnMetaDataList.stream()
                    .map(columnMetaData -> new InsertV2.InsertColumn(columnMetaData.name(), null))
                    .toList();
            ensureUpdateColumns().addAll(updateColumns);
        }

        public void addUpdateColumn(final ColumnMetaData column) {
            ensureColumnMetaDataList().add(column);
            ensureUpdateColumns().add(new InsertV2.InsertColumn(column.name(), null));
        }

        public @Nullable List<InsertV2.InsertColumn> getUpdateColumns() {
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

        private List<InsertV2.InsertColumn> ensureUpdateColumns() {
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
