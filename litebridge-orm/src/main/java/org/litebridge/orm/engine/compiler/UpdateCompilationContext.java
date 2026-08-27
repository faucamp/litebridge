package org.litebridge.orm.engine.compiler;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.expression.BindValueExpression;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.expression.ColumnExpression;
import org.litebridge.db.spi.expression.LiteralExpression;
import org.litebridge.db.spi.expression.SelectExpression;
import org.litebridge.db.spi.expression.SelectReference;
import org.litebridge.db.spi.expression.SubselectExpression;
import org.litebridge.db.spi.math.MathOperation;
import org.litebridge.db.spi.query.Condition;
import org.litebridge.db.spi.query.ConditionGroup;
import org.litebridge.db.spi.query.LogicCondition;
import org.litebridge.db.spi.query.LogicConditionGroup;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.db.spi.query.Select;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.db.spi.update.Update;
import org.litebridge.db.spi.update.UpdateColumn;
import org.litebridge.orm.api.select.ast.ConditionNode;
import org.litebridge.orm.api.select.ast.SetNode;
import org.litebridge.orm.api.select.ast.UpdateNode;
import org.litebridge.orm.api.select.model.ConditionGroupSpec;
import org.litebridge.orm.api.select.model.ConditionSpec;
import org.litebridge.orm.api.select.model.SelectExpressionMapper;
import org.litebridge.orm.api.select.model.SelectSpec;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ColumnExpressionSpec;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.meta.QueryField;
import org.litebridge.orm.meta.QueryFieldInspector;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.TableMetaDataCache;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class UpdateCompilationContext extends AbstractCompilationContext {

    private final Table table;
    private final TableMetaData tableMetaData;
    private final @Nullable OrmTable ormTable;
    private final List<SetNode> setNodes = new ArrayList<>();
    private final List<BindValue> bindValues = new ArrayList<>();
    private @Nullable ConditionGroupSpecStack where;

    public UpdateCompilationContext(final UpdateNode updateNode,
                                    final LitebridgeContext litebridgeContext) {
        super(litebridgeContext);

        if (updateNode.dtoClass() != null) {
            this.ormTable = litebridgeContext.tableRegistry().getTableOrThrow(updateNode.dtoClass());
            this.tableMetaData = ormTable.getMetaData();
            this.table = tableMetaData.toTable();
        } else {
            this.ormTable = null;
            this.table = litebridgeContext.tableRegistry().getOrCreateSpiTable(Objects.requireNonNull(updateNode.table()));
            this.tableMetaData = litebridgeContext.tableMetaDataCache().ensureTableMetaData(table);
        }
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

    public void addCondition(final ConditionNode conditionNode) {
        ensureWhereConditionGroupStack().current().newCondition(conditionNode.logicOperator(),
                conditionNode.lhsColumn(),
                conditionNode.lhsExpression(),
                conditionNode.operator(),
                conditionNode.rhs());
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

        final ConditionGroup conditionGroup = toConditionGroup(where.current(), ormTable, table);
        return new Update(table, updateColumns, conditionGroup);
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
