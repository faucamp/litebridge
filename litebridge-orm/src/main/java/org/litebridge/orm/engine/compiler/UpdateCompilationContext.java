package org.litebridge.orm.engine.compiler;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.math.MathOperation;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.db.spi.update.Update;
import org.litebridge.db.spi.update.UpdateColumn;
import org.litebridge.orm.api.select.ast.ConditionNode;
import org.litebridge.orm.api.select.ast.SetNode;
import org.litebridge.orm.api.select.ast.UpdateNode;
import org.litebridge.orm.api.select.model.SelectExpressionMapper;
import org.litebridge.orm.expression.ColumnExpressionSpec;
import org.litebridge.orm.meta.QueryField;
import org.litebridge.orm.meta.QueryFieldInspector;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.TableMetaDataCache;
import org.litebridge.orm.persistence.TableRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class UpdateCompilationContext implements CompilationContext {

    private final Table table;
    private final TableMetaData tableMetaData;
    private final OrmTable ormTable;
    private final SelectExpressionMapper selectExpressionMapper;
    private final TableMetaDataCache tableMetaDataCache;
    private final TypeConverter typeConverter;
    private final List<SetNode> setNodes = new ArrayList<>();
    private final List<BindValue> bindValues = new ArrayList<>();
    private @Nullable ConditionGroupSpecStack where;

    public UpdateCompilationContext(final UpdateNode updateNode,
                                    final SelectExpressionMapper selectExpressionMapper,
                                    final TableRegistry tableRegistry,
                                    final TableMetaDataCache tableMetaDataCache,
                                    final TypeConverter typeConverter) {
        this.selectExpressionMapper = selectExpressionMapper;
        this.tableMetaDataCache = tableMetaDataCache;

        if (updateNode.dtoClass() != null) {
            this.ormTable = tableRegistry.getTableOrThrow(updateNode.dtoClass());
            this.tableMetaData = ormTable.getMetaData();
            this.table = tableMetaData.toTable();
        } else {
            this.ormTable = null;
            this.table = tableRegistry.getOrCreateSpiTable(Objects.requireNonNull(updateNode.table()));
            this.tableMetaData = tableMetaDataCache.ensureTableMetaData(table);
        }

        this.typeConverter = typeConverter;
    }

    public void addSetNode(final SetNode setNode) {
        setNodes.add(setNode);
    }

    public ConditionGroupSpecStack ensureWhereConditionGroupStack() {
        if (where == null) {
            where = new ConditionGroupSpecStack();
        }

        return where;
    }

    public @Nullable ConditionGroupSpecStack getWhereConditionGroupStack() {
        return where;
    }

    public void addWhereCondition(final ConditionNode conditionNode) {
        ensureWhereConditionGroupStack().current().newCondition(conditionNode.logicOperator(), conditionNode.lhs(), conditionNode.operator());
        //TODO: fix datatype
        final int sqlDataType = 0;
        final BindValue bindValue = new BindValue(conditionNode.rhs(), sqlDataType);
        bindValues.add(bindValue);
    }

    @Override
    public List<BindValue> getBindValues() {
        return bindValues != null ? bindValues : Collections.emptyList();
    }

    @Override
    public Update toOperation() {
        final List<UpdateColumn> updateColumns;
        final List<BindValue> bindValues = new ArrayList<>();

        if (ormTable != null) {
            updateColumns = setNodes.stream()
                    .map(setNode -> {
                        final String fieldName = getColumn(setNode);
                        final ColumnMetaData columnMetaData = ormTable.getColumnForFieldName(fieldName);

                        if (setNode.value() instanceof MathOperation mathOperation) {
                            final BindValue bindValue = new BindValue(mathOperation.value(), columnMetaData.getDataType());
                            bindValues.add(bindValue);
                            return new UpdateColumn(columnMetaData.name(), null, mathOperation);
                        } else {
                            final BindValue bindValue = new BindValue(setNode.value(), columnMetaData.getDataType());
                            bindValues.add(bindValue);
                            return new UpdateColumn(columnMetaData.name());
                        }
                    })
                    .toList();
        } else {
            updateColumns = setNodes.stream()
                    .map(setNode -> {
                        final String columnName = getColumn(setNode);
                        final ColumnMetaData columnMetaData = tableMetaData.column(columnName);

                        if (setNode.value() instanceof MathOperation mathOperation) {
                            final BindValue bindValue = new BindValue(mathOperation.value(), columnMetaData.getDataType());
                            bindValues.add(bindValue);
                            return new UpdateColumn(columnName, null, mathOperation);
                        } else {
                            final BindValue bindValue = new BindValue(setNode.value(), columnMetaData.getDataType());
                            bindValues.add(bindValue);
                            return new UpdateColumn(columnName, null);
                        }
                    })
                    .toList();
        }

        if (this.bindValues != null) {
            bindValues.addAll(this.bindValues);
            this.bindValues.clear();
            this.bindValues.addAll(bindValues);
        }

        return new Update(table, updateColumns, where.current().toConditionGroup(selectExpressionMapper,
                Set.of(table),
                bindValues,
                tableMetaDataCache,
                typeConverter));
    }

    private static String getColumn(final SetNode setNode) {
        if (setNode.column() != null) {
            return setNode.column();
        } else {
            return switch (Objects.requireNonNull(setNode.expressionSpec())) {
                case ColumnExpressionSpec columnExpressionSpec -> columnExpressionSpec.getColumn().name();
                case QueryField queryField -> QueryFieldInspector.getFieldName(queryField);
                default -> throw new IllegalStateException("Unsupported expression spec: " + setNode.expressionSpec());
            };
        }
    }
}
