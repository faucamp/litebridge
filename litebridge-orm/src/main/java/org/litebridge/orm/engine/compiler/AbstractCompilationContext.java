package org.litebridge.orm.engine.compiler;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.VirtualTable;
import org.litebridge.db.spi.VirtualTableMetaData;
import org.litebridge.db.spi.alias.Aliased;
import org.litebridge.db.spi.alias.AliasedQuery;
import org.litebridge.db.spi.alias.AliasedTable;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.expression.BindValueExpression;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.expression.ColumnExpression;
import org.litebridge.db.spi.expression.ColumnReference;
import org.litebridge.db.spi.expression.SelectExpression;
import org.litebridge.db.spi.expression.SubselectExpression;
import org.litebridge.db.spi.query.Condition;
import org.litebridge.db.spi.query.ConditionGroup;
import org.litebridge.db.spi.query.LogicCondition;
import org.litebridge.db.spi.query.LogicConditionGroup;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.db.spi.query.Select;
import org.litebridge.db.spi.query.SelectTarget;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.orm.api.select.model.ConditionGroupSpec;
import org.litebridge.orm.api.select.model.ConditionSpec;
import org.litebridge.orm.api.select.model.SelectExpressionMapper;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.ProtoColumnExpressionSpec;
import org.litebridge.orm.expression.ProtoExpressionSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;
import org.litebridge.orm.meta.QueryField;
import org.litebridge.orm.meta.QueryFieldInspector;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.TableMetaDataCache;
import org.litebridge.orm.persistence.TableRegistry;
import org.litebridge.orm.persistence.alias.AliasGenerator;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

abstract sealed class AbstractCompilationContext implements CompilationContext permits DeleteCompilationContext, MergeCompilationContext, SelectCompilationContext, UpdateCompilationContext {

    protected final LitebridgeContext litebridgeContext;
    protected final AliasGenerator aliasGenerator;
    protected final TableRegistry tableRegistry;
    protected final List<BindValue> bindValues = new ArrayList<>();
    private @Nullable Map<String, VirtualTable> virtualTableMap;

    protected AbstractCompilationContext(final LitebridgeContext litebridgeContext) {
        this.litebridgeContext = litebridgeContext;
        this.aliasGenerator = litebridgeContext.aliasGenerator();
        this.tableRegistry = litebridgeContext.tableRegistry();
    }

    @Override
    public List<BindValue> getBindValues() {
        return bindValues;
    }

    protected final ConditionGroup toConditionGroup(final ConditionGroupSpec conditionGroupSpec, final SelectTarget selectTargets) {
        return toConditionGroup(conditionGroupSpec, Collections.singletonList(selectTargets));
    }

    protected final ConditionGroup toConditionGroup(final ConditionGroupSpec conditionGroupSpec, final List<SelectTarget> selectTargets) {
        final List<LogicCondition> resolvedConditions = conditionGroupSpec.conditions().stream()
                .map(spec -> new LogicCondition(spec.logicOperator(),
                        toCondition(spec.conditionSpec(), selectTargets)))
                .toList();

        final List<LogicConditionGroup> subConditionGroups = conditionGroupSpec.subgroups().stream()
                .map(subgroup -> {
                    final ConditionGroup conditionGroup = toConditionGroup(subgroup.conditionGroupSpec(), selectTargets);
                    return new LogicConditionGroup(subgroup.logicOperator(), conditionGroup);
                })
                .toList();

        return new ConditionGroup(resolvedConditions, subConditionGroups);
    }

    protected Condition toCondition(final ConditionSpec conditionSpec, final List<SelectTarget> selectTargets) {
        final SelectExpressionMapper selectExpressionMapper = litebridgeContext.selectExpressionMapper();
        final Operator operator = conditionSpec.getOperator();
        //TODO: fix
//        final SelectTarget selectTarget = selectTargets.getFirst();
//        final Table table = getTable(selectTarget);
//        final String tableAlias = operator == Operator.USING ? null : getAlias(selectTarget);
        ExpressionSpec lhsExpressionSpec;

        // Compile condition specs
        if (conditionSpec.getLhsExpression() != null) {
            // Expression specification
            lhsExpressionSpec = conditionSpec.getLhsExpression();
            final Table table;
            final OrmTable ormTable;
            final SelectTarget selectTarget;

            if (lhsExpressionSpec instanceof ProtoExpressionSpec || lhsExpressionSpec instanceof QueryField) {
                final Class<?> dtoClass;

                if (lhsExpressionSpec instanceof QueryField queryField) {
                    dtoClass = QueryFieldInspector.getDtoClass(queryField);
                } else if (lhsExpressionSpec instanceof ProtoColumnExpressionSpec protoColumnExpressionSpec
                        && protoColumnExpressionSpec.args() != null) {
                    dtoClass = (Class<?>) protoColumnExpressionSpec.args()[0];
                } else {
                    dtoClass = null;
                }

                if (dtoClass != null) {
                    ormTable = tableRegistry.getOrmTable(dtoClass);
                    table = ormTable.getMetaData().table();
                    selectTarget = selectTargets.stream()
                            .filter(st -> st.equals(table))
                            .findFirst().orElseThrow();
                } else {
                    selectTarget = selectTargets.getFirst();
                    table = getTable(selectTarget);
                    ormTable = tableRegistry.getOrmTable(table);
                }

                final String tableAlias = operator == Operator.USING ? null : getAlias(selectTarget);
                final List<ExpressionSpec> lhsResolvedExpressionSpecs = selectExpressionMapper
                        .resolveProtoExpression(lhsExpressionSpec, ormTable, table, tableAlias, ClauseType.WHERE).stream()
                        .toList();

                if (lhsResolvedExpressionSpecs.size() != 1) {
                    throw new IllegalArgumentException("Expected exactly one LHS expression spec, but got " + lhsResolvedExpressionSpecs.size());
                }

                lhsExpressionSpec = lhsResolvedExpressionSpecs.getFirst();
            } else {
                lhsExpressionSpec = conditionSpec.getLhsExpression();
            }
        } else if (litebridgeContext.mode() == LitebridgeContext.Mode.DTO) {
            // DTO field name
            final SelectTarget selectTarget = selectTargets.getFirst();
            final Table table = getTable(selectTarget);
            final String tableAlias = operator == Operator.USING ? null : getAlias(selectTarget);

            final OrmTable ormTable = tableRegistry.getOrmTableOrThrow(table);
            final ColumnMetaData columnMetaData = ormTable.columnMetaDataForField(Objects.requireNonNull(conditionSpec.getLhsColumn()));
            final Column column = columnMetaData.column();
            final String columnAlias = aliasGenerator.columnAlias(column);
            lhsExpressionSpec = new SelectColumnSpec(column, columnAlias, tableAlias);
        } else {
            // Column name
            final TableMetaDataCache tableMetaDataCache = litebridgeContext.tableMetaDataCache();
            final String columnName = Objects.requireNonNull(conditionSpec.getLhsColumn());
            SelectTarget selectTarget = null;
            Table table = null;

            for (SelectTarget st : selectTargets) {
                final Table t = getTable(st);

                if (t.isVirtual()) {
                    // Fallback on the last seen virtual table if no real table match was found for this column
                    selectTarget = st;
                    table = t;
                    continue;
                }

                final TableMetaData tableMetaData = tableMetaDataCache.ensureTableMetaData(t);

                if (tableMetaData.hasColumn(columnName)) {
                    // Column found in select target table
                    selectTarget = st;
                    table = t;
                    break;
                }
            }

            if (selectTarget == null) {
                throw new IllegalArgumentException("No such column: " + conditionSpec.getLhsColumn());
            }

            final String tableAlias = operator == Operator.USING ? null : getAlias(selectTarget);

            final Column column = new Column(table, Objects.requireNonNull(conditionSpec.getLhsColumn()));
            final String columnAlias = aliasGenerator.columnAlias(column);
            lhsExpressionSpec = new SelectColumnSpec(column, columnAlias, tableAlias);
        }

        final SelectExpression lhsSelectExpression = selectExpressionMapper.toSelectExpression(lhsExpressionSpec, true);
        final Object value = conditionSpec.getValue();

        if (value instanceof QueryNode subselectNode) {
            // Subselect
            final QueryCompiler queryCompiler = litebridgeContext.createQueryCompiler();
            final PreparedOperation preparedOperation = queryCompiler.compile(subselectNode);
            bindValues.addAll(preparedOperation.bindValues());
            final Select subselect = (Select) preparedOperation.operation();
            final SubselectExpression subselectExpression = litebridgeContext.sqlFunctionRegistry().select().subselect().create(subselect);
            return new Condition(lhsSelectExpression, operator, subselectExpression);
        } else if (value instanceof ExpressionSpec expressionSpec) {
            ExpressionSpec rhsExpressionSpec;

            if (expressionSpec instanceof ProtoExpressionSpec || expressionSpec instanceof QueryField) {
                final SelectTarget selectTarget = selectTargets.getFirst();
                final Table table = getTable(selectTarget);
                final String tableAlias = operator == Operator.USING ? null : getAlias(selectTarget);

                final OrmTable ormTable = tableRegistry.getOrmTable(table);
                final List<ExpressionSpec> rhsResolvedExpressionSpecs = selectExpressionMapper.resolveProtoExpression(expressionSpec, ormTable, table, tableAlias, ClauseType.WHERE);

                if (rhsResolvedExpressionSpecs.size() != 1) {
                    throw new IllegalArgumentException("Expected exactly one RHS expression spec, but got " + rhsResolvedExpressionSpecs.size());
                }

                rhsExpressionSpec = rhsResolvedExpressionSpecs.getFirst();
            } else {
                rhsExpressionSpec = expressionSpec;
            }

            return new Condition(lhsSelectExpression, operator, selectExpressionMapper.toSelectExpression(rhsExpressionSpec, true));
        } else if (value instanceof Column referencedColumn) {
            // Reference to a selected column
            //TODO: alias regression
            final ColumnReference columnReference = litebridgeContext.sqlFunctionRegistry().select().reference().create(referencedColumn, null, null);
            return new Condition(lhsSelectExpression, operator, columnReference);
        }

        // Store bind values and return condition
        return switch (operator) {
            case USING -> {
                final ColumnReference lhsColumnRef = (ColumnReference) lhsSelectExpression;
                final ColumnReference usingColumRef = litebridgeContext.sqlFunctionRegistry().select().reference().create(new Column(lhsColumnRef.column().name(), null), null, null);
                yield new Condition(usingColumRef, operator, usingColumRef);
            }
            case IS_NULL, IS_NOT_NULL -> new Condition(lhsSelectExpression, operator, null);
            default -> {
                final BindValueExpression bindValueExpression = createBindValueExpression(value, bindValues.size());
                bindValues.addAll(createBindValues(lhsSelectExpression, value, litebridgeContext.tableMetaDataCache(), litebridgeContext.typeConverter()));
                yield new Condition(lhsSelectExpression, operator, bindValueExpression);
            }
        };
    }

    /**
     * Creates a bind value for a column and raw value.
     *
     * @param lhsSelectExpression LHS select expression for the condition.
     * @param rawValue            The raw value.
     * @param tableMetaDataCache  Table metadata cache.
     * @return The bind value.
     */
    protected List<BindValue> createBindValues(final SelectExpression lhsSelectExpression, final @Nullable Object rawValue, final TableMetaDataCache tableMetaDataCache, final TypeConverter typeConverter) {
        final Column column;

        if (lhsSelectExpression instanceof ColumnExpression columnExpression) {
            column = columnExpression.column();
        } else {
            column = null;
        }

        if (column != null) {
            final ColumnMetaData columnMetaData = getTableMetaData(column.table()).column(column.name());

            if (rawValue instanceof Collection<?> collection) {
                // Multiple bind values
                return collection.stream()
                        .map(value -> typeConverter.convert(value, columnMetaData.getDataType()))
                        .map(convertedValue -> new BindValue(convertedValue, columnMetaData.getDataType()))
                        .toList();
            } else {
                // Single bind value
                final Object convertedValue = typeConverter.convert(rawValue, columnMetaData.getDataType());
                return Collections.singletonList(new BindValue(convertedValue, columnMetaData.getDataType()));
            }
        } else if (rawValue != null) {
            if (rawValue instanceof Collection<?> collection) {
                // Multiple bind values
                return collection.stream()
                        .map(value -> new BindValue(value, typeConverter.getSqlDataType(value.getClass())))
                        .toList();
            } else {
                // Single bind value
                return Collections.singletonList(new BindValue(rawValue, typeConverter.getSqlDataType(rawValue.getClass())));
            }
        } else {
            return Collections.singletonList(new BindValue(null, Types.NULL));
        }
    }

    protected final @Nullable OrmTable getOrmTableIfNotNull(final @Nullable Class<?> dtoClass, final @Nullable Class<?> contextDtoClass) {
        if (dtoClass != null) {
            return getOrmTable(dtoClass, contextDtoClass);
        } else {
            return null;
        }
    }

    protected final OrmTable getOrmTable(final Class<?> dtoClass, final @Nullable Class<?> contextDtoClass) {
        final OrmTable ormTable;

        if (contextDtoClass != null) {
            ormTable = tableRegistry.getOrmTableInContextOrThrow(dtoClass, contextDtoClass);
        } else {
            ormTable = tableRegistry.getOrmTableOrThrow(dtoClass);
        }

        return ormTable;
    }

    protected final Table getTable(final SelectTarget selectTarget) {
        return switch (selectTarget) {
            case Table spiTable -> spiTable;
            case AliasedTable aliasedTable -> aliasedTable.target();
            default -> {
                // Virtual table
                if (selectTarget instanceof AliasedQuery aliasedQuery) {
                    if (virtualTableMap == null) {
                        virtualTableMap = new HashMap<>();
                    }

                    yield virtualTableMap.computeIfAbsent(aliasedQuery.alias(), VirtualTable::new);
                } else {
                    yield VirtualTable.anonymous();
                }
            }
        };
    }

    protected final TableMetaData getTableMetaData(final Table table) {
        if (table instanceof VirtualTable virtualTable) {
            return new VirtualTableMetaData(virtualTable);
        }

        return litebridgeContext.tableMetaDataCache().ensureTableMetaData(table);
    }

    protected final SelectTarget getSelectTargetDto(final Class<?> dtoClass,
                                                    final @Nullable Class<?> contextDtoClass,
                                                    final @Nullable String alias) {
        final OrmTable ormTable = getOrmTable(dtoClass, contextDtoClass);
        final Table table = ormTable.getMetaData().table();
        final String tableAlias = alias != null ? alias : aliasGenerator.newTableAlias(table);
        return new AliasedTable(tableAlias, table);
    }

    protected final SelectTarget getSelectTargetTable(final String tableName, final @Nullable String alias) {
        final Table table = tableRegistry.getOrCreateSpiTable(tableName);

        if (alias != null) {
            return new AliasedTable(alias, table);
        } else {
            return table;
        }
    }

    protected final SelectTarget getSelectTargetQuery(final QueryNode fromQueryNode, final @Nullable String alias) {
        final SelectTarget query;
        litebridgeContext.aliasGenerator().pushScope();
        final PreparedOperation preparedOperation = litebridgeContext.createQueryCompiler().compile(fromQueryNode);
        litebridgeContext.aliasGenerator().popScope();
        bindValues.addAll(preparedOperation.bindValues());

        if (alias != null) {
            query = new AliasedQuery(alias, (Select) preparedOperation.operation());
        } else {
            query = (Select) preparedOperation.operation();
        }

        return query;
    }

    protected @Nullable String getAlias(final SelectTarget selectTarget) {
        return switch (selectTarget) {
            case Aliased<?> aliased -> aliased.alias();
            case Table table -> aliasGenerator.tableAlias(table);
            default -> null;
        };
    }

    protected static BindValueExpression createBindValueExpression(final @Nullable Object value, final int index) {
        final int valueSize;

        if (value instanceof Collection<?> collection) {
            valueSize = collection.size();
        } else {
            valueSize = 1;
        }

        return new BindValueExpression(index, valueSize);
    }
}
