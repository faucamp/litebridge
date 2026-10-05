package org.litebridge.orm.engine.compiler;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.query.ConditionGroup;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.db.spi.update.Update;
import org.litebridge.db.spi.update.UpdateColumn;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.ConditionNode;
import org.litebridge.orm.engine.ast.SetNode;
import org.litebridge.orm.engine.ast.UpdateNode;
import org.litebridge.orm.expression.ColumnExpressionSpec;
import org.litebridge.orm.meta.QueryField;
import org.litebridge.orm.meta.QueryFieldInspector;
import org.litebridge.db.spi.MappedFieldTarget;
import org.litebridge.orm.persistence.MappedCompositeKey;
import org.litebridge.orm.persistence.OrmTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Compilation context for UPDATE statements.
 */
final class UpdateCompilationContext extends AbstractCompilationContext {

    private static final ConditionGroup EMPTY_CONDITION_GROUP = new ConditionGroup(List.of());

    private final Table table;
    private final TableMetaData tableMetaData;
    private final @Nullable OrmTable ormTable;
    private final List<SetNode> setNodes = new ArrayList<>();
    private @Nullable ConditionGroupSpecStack where;

    UpdateCompilationContext(final UpdateNode updateNode,
                             final LitebridgeContext litebridgeContext) {
        super(litebridgeContext);

        if (updateNode.dtoClass() != null) {
            this.ormTable = getOrmTablePermissiveContext(updateNode.dtoClass(), updateNode.contextDtoClass());
            this.tableMetaData = ormTable.getMetaData();
            this.table = tableMetaData.table();
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
    }

    @Override
    public Update toOperation() {
        final List<UpdateColumn> updateColumns;
        final List<BindValue> bindValues = new ArrayList<>();

        if (ormTable != null) {
            updateColumns = setNodes.stream()
                    .flatMap(setNode -> {
                        final String fieldName = getColumn(setNode);
                        final MappedFieldTarget target = ormTable.mappedFieldTargetForFieldOrNull(fieldName);
                        if (target instanceof MappedCompositeKey mappedCompositeKey) {
                            final List<UpdateColumn> result = new ArrayList<>();
                            for (MappedFieldTarget mft : mappedCompositeKey.columns()) {
                                if (mft instanceof ColumnMetaData cmd) {
                                    final Object val;
                                    if (setNode.value() instanceof Map<?, ?> map) {
                                        final String joinColName = cmd.getJoinColumn() != null ? cmd.getJoinColumn().name() : null;
                                        final String pkFieldName = (mappedCompositeKey.targetOrmTable() != null && joinColName != null)
                                                ? mappedCompositeKey.targetOrmTable().get().getFieldForColumnName(joinColName).name()
                                                : null;

                                        if (map.containsKey(cmd.name())) {
                                            val = map.get(cmd.name());
                                        } else if (joinColName != null && map.containsKey(joinColName)) {
                                            val = map.get(joinColName);
                                        } else if (pkFieldName != null && map.containsKey(pkFieldName)) {
                                            val = map.get(pkFieldName);
                                        } else {
                                            val = null;
                                        }
                                    } else {
                                        val = setNode.value();
                                    }
                                    bindValues.add(new BindValue(val, cmd.getDataType()));
                                    result.add(new UpdateColumn(cmd.name(), null, setNode.mathOperator()));
                                }
                            }
                            return result.stream();
                        }
                        final ColumnMetaData columnMetaData = ormTable.columnMetaDataForField(fieldName);
                        bindValues.add(new BindValue(setNode.value(), columnMetaData.getDataType()));
                        return Stream.of(new UpdateColumn(columnMetaData.name(), null, setNode.mathOperator()));
                    })
                    .toList();
        } else {
            updateColumns = setNodes.stream()
                    .map(setNode -> {
                        final String columnName = getColumn(setNode);
                        final ColumnMetaData columnMetaData = tableMetaData.column(columnName);
                        bindValues.add(new BindValue(setNode.value(), columnMetaData.getDataType()));
                        return new UpdateColumn(columnName, null, setNode.mathOperator());
                    })
                    .toList();
        }

        bindValues.addAll(this.bindValues);
        this.bindValues.clear();
        this.bindValues.addAll(bindValues);

        final ConditionGroup conditionGroup = where != null ? toConditionGroup(where.current(), table, EMPTY_SELECT_EXPRESSIONS) : EMPTY_CONDITION_GROUP;
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
