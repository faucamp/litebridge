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
import org.litebridge.orm.expression.ColumnExpressionSpec;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.ProtoColumnExpressionSpec;
import org.litebridge.orm.expression.ProtoExpressionSpec;
import org.litebridge.orm.expression.Resolvable;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

abstract sealed class AbstractCompilationContext implements CompilationContext permits DeleteCompilationContext, MergeCompilationContext, SelectCompilationContext, UpdateCompilationContext {

    protected static final SelectExpressions EMPTY_SELECT_EXPRESSIONS = new SelectExpressions(Collections.emptyList(), Collections.emptyMap());
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

    protected final ConditionGroup toConditionGroup(final ConditionGroupSpec conditionGroupSpec, final SelectTarget selectTargets, final SelectExpressions selectExpressions) {
        return toConditionGroup(conditionGroupSpec, Collections.singletonList(selectTargets), selectExpressions);
    }

    protected final ConditionGroup toConditionGroup(final ConditionGroupSpec conditionGroupSpec, final List<SelectTarget> selectTargets, final SelectExpressions selectExpressions) {
        final List<LogicCondition> resolvedConditions = conditionGroupSpec.conditions().stream()
                .map(spec -> new LogicCondition(spec.logicOperator(),
                        toCondition(spec.conditionSpec(), selectTargets, selectExpressions)))
                .toList();

        final List<LogicConditionGroup> subConditionGroups = conditionGroupSpec.subgroups().stream()
                .map(subgroup -> {
                    final ConditionGroup conditionGroup = toConditionGroup(subgroup.conditionGroupSpec(), selectTargets, selectExpressions);
                    return new LogicConditionGroup(subgroup.logicOperator(), conditionGroup);
                })
                .toList();

        return new ConditionGroup(resolvedConditions, subConditionGroups);
    }

    protected Condition toCondition(final ConditionSpec conditionSpec, final List<SelectTarget> selectTargets, final SelectExpressions selectExpressions) {
        final SelectExpressionMapper selectExpressionMapper = litebridgeContext.selectExpressionMapper();
        final Operator operator = Objects.requireNonNull(conditionSpec.operator());
        ExpressionSpec lhsExpressionSpec;

        // Compile condition specs
        if (conditionSpec.lhsExpression() != null) {
            // Expression specification
            lhsExpressionSpec = resolveConditionExpressionSpec(conditionSpec.lhsExpression(), selectTargets, operator);
        } else if (conditionSpec.lhsColumn() != null) {
            final String lhsColName = conditionSpec.lhsColumn();
            final SelectTarget selectTarget = findSelectTarget(lhsColName, selectTargets);
            final Table table = getTable(selectTarget);
            final String tableAlias = operator == Operator.USING ? null : getAlias(selectTarget);

            if (litebridgeContext.mode() == LitebridgeContext.Mode.DTO) {
                // DTO field name
                final OrmTable ormTable = tableRegistry.getOrmTableOrThrow(table);
                final ColumnMetaData columnMetaData = ormTable.columnMetaDataForField(Objects.requireNonNull(lhsColName));
                final Column column = columnMetaData.column();
                final String columnAlias = aliasGenerator.columnAlias(column);
                lhsExpressionSpec = new SelectColumnSpec(column, columnAlias, tableAlias);
            } else {
                // Column name
                final Column column = new Column(table, Objects.requireNonNull(lhsColName));
                final String columnAlias = aliasGenerator.columnAlias(column);
                lhsExpressionSpec = new SelectColumnSpec(column, columnAlias, tableAlias);
            }
        } else if (operator == Operator.EXISTS) {
            final QueryNode subselectNode = (QueryNode) Objects.requireNonNull(conditionSpec.value(), "No EXISTS subquery specified");
            return createSubSelectCondition(subselectNode, null, operator, selectTargets);
        } else {
            throw new IllegalArgumentException("Invalid condition spec: " + conditionSpec);
        }

        final SelectExpression lhsSelectExpression = selectExpressionMapper.toSelectExpression(lhsExpressionSpec, selectExpressions.aliases());
        final Object value = conditionSpec.value();

        if (value instanceof QueryNode subselectNode) {
            // Subselect
            return createSubSelectCondition(subselectNode, lhsSelectExpression, operator, selectTargets);
        } else if (value instanceof ExpressionSpec expressionSpec) {
            final ExpressionSpec rhsExpressionSpec = resolveConditionExpressionSpec(expressionSpec, selectTargets, operator);
            return new Condition(lhsSelectExpression, operator, selectExpressionMapper.toSelectExpression(rhsExpressionSpec, selectExpressions.aliases()));
        } else if (value instanceof Column referencedColumn) {
            // Reference to a selected column
            //TODO: alias regression
            final ColumnReference columnReference = litebridgeContext.sqlFunctionRegistry().select().reference().create(referencedColumn, null, null);
            return new Condition(lhsSelectExpression, operator, columnReference);
        }

        // Store bind values and return condition
        return switch (operator) {
            case USING -> {
                final ColumnExpression lhsColumnExpression = (ColumnExpression) lhsSelectExpression;
                final ColumnExpression usingColumExpression = litebridgeContext.sqlFunctionRegistry().select().column().create(new Column(lhsColumnExpression.column().name()), null, null);
                yield new Condition(usingColumExpression, operator, usingColumExpression);
            }
            case IS_NULL, IS_NOT_NULL -> new Condition(lhsSelectExpression, operator, null);
            default -> {
                final BindValueExpression bindValueExpression = createBindValueExpression(value, bindValues.size());
                bindValues.addAll(createBindValues(lhsSelectExpression, value, litebridgeContext.tableMetaDataCache(), litebridgeContext.typeConverter()));
                yield new Condition(lhsSelectExpression, operator, bindValueExpression);
            }
        };
    }

    protected Condition createSubSelectCondition(final QueryNode subselectNode,
                                                 final @Nullable SelectExpression lhsSelectExpression,
                                                 final Operator operator,
                                                 final List<SelectTarget> selectTargets) {
        final QueryCompiler queryCompiler = litebridgeContext.createQueryCompiler();
        final PreparedOperation preparedOperation = queryCompiler.compile(subselectNode, selectTargets);
        bindValues.addAll(preparedOperation.bindValues());
        final Select subselect = (Select) preparedOperation.operation();
        final SubselectExpression subselectExpression = litebridgeContext.sqlFunctionRegistry().select().subselect().create(subselect);
        return new Condition(lhsSelectExpression, operator, subselectExpression);
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
        return getOrmTable(dtoClass, contextDtoClass, null);
    }

    protected final OrmTable getOrmTable(final Class<?> dtoClass, final @Nullable Class<?> contextDtoClass, final @Nullable List<Class<?>> fallbackContextDtoClasses) {
        OrmTable ormTable;

        if (contextDtoClass != null) {
            ormTable = tableRegistry.getOrmTableInContextOrThrow(dtoClass, contextDtoClass);
        } else if (fallbackContextDtoClasses != null) {
            ormTable = tableRegistry.getOrmTable(dtoClass);

            if (ormTable == null) {
                // Traverse the fallbacks to infer a possible context, latest one first
                for (Class<?> fallbackContextDtoClass : fallbackContextDtoClasses.reversed()) {
                    ormTable = tableRegistry.getOrmTableInContext(dtoClass, fallbackContextDtoClass);

                    if (ormTable != null) {
                        break;
                    }
                }
            }

            if (ormTable == null) {
                throw new IllegalArgumentException("DTO class not registered and context could not be inferred: '%s'".formatted(dtoClass.getName()));
            }
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
                                                    final @Nullable List<Class<?>> fallbackContextDtoClasses,
                                                    final @Nullable String alias) {
        final OrmTable ormTable = getOrmTable(dtoClass, contextDtoClass, fallbackContextDtoClasses);
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
        if (selectTarget instanceof Aliased<?> aliased) {
            return aliased.alias();
        }

        return null;
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

    private TargetResolution resolveSelectTarget(final ExpressionSpec expressionSpec,
                                                 final List<SelectTarget> selectTargets,
                                                 final Operator operator) {
        final Class<?> dtoClass;

        if (expressionSpec instanceof QueryField queryField) {
            dtoClass = QueryFieldInspector.getDtoClass(queryField);
        } else if (expressionSpec instanceof ProtoColumnExpressionSpec protoColumnExpressionSpec
                && protoColumnExpressionSpec.args() != null
                && protoColumnExpressionSpec.args().length > 0
                && protoColumnExpressionSpec.args()[0] instanceof Class<?> clazz) {
            dtoClass = clazz;
        } else {
            dtoClass = null;
        }

        final Table table;
        final OrmTable ormTable;
        final SelectTarget selectTarget;

        if (dtoClass != null) {
            OrmTable foundOrmTable = tableRegistry.getOrmTable(dtoClass);
            SelectTarget foundSelectTarget = null;

            if (foundOrmTable != null) {
                final Table t = foundOrmTable.getMetaData().table();
                foundSelectTarget = selectTargets.stream()
                        .filter(st -> getTable(st).equals(t))
                        .findFirst()
                        .orElse(null);
            } else {
                for (final SelectTarget st : selectTargets) {
                    final Table t = getTable(st);
                    final OrmTable stOrm = tableRegistry.getOrmTable(t);
                    if (stOrm != null && dtoClass.equals(stOrm.dtoClass())) {
                        foundOrmTable = stOrm;
                        foundSelectTarget = st;
                        break;
                    }
                }
            }

            if (foundOrmTable == null || foundSelectTarget == null) {
                throw new IllegalArgumentException("Target table not found for DTO: " + dtoClass);
            }

            ormTable = foundOrmTable;
            table = ormTable.getMetaData().table();
            selectTarget = foundSelectTarget;
        } else {
            selectTarget = selectTargets.getFirst();
            table = getTable(selectTarget);
            ormTable = tableRegistry.getOrmTable(table);
        }

        final String tableAlias = operator == Operator.USING ? null : getAlias(selectTarget);
        return new TargetResolution(selectTarget, table, ormTable, tableAlias);
    }

    protected final boolean matchesSelectTarget(final ExpressionSpec expressionSpec, final SelectTarget selectTarget) {
        return switch (expressionSpec) {
            case QueryField queryField -> {
                final Class<?> dtoClass = QueryFieldInspector.getDtoClass(queryField);
                final OrmTable ormTable = tableRegistry.getOrmTable(dtoClass);

                if (ormTable != null) {
                    final Table table = ormTable.getMetaData().table();
                    yield table.equals(getTable(selectTarget));
                } else {
                    final Table table = getTable(selectTarget);
                    final OrmTable stOrm = tableRegistry.getOrmTable(table);
                    yield stOrm != null && dtoClass.equals(stOrm.dtoClass());
                }
            }
            case ColumnExpressionSpec columnExpressionSpec -> {
                final String tableAlias = columnExpressionSpec.getTableAlias();

                if (tableAlias == null) {
                    yield matchesSelectTarget(columnExpressionSpec.getColumn().name(), selectTarget);
                }

                yield tableAlias.equals(getAlias(selectTarget));
            }
            case Resolvable resolvable -> matchesSelectTarget(resolvable.column(), selectTarget);
            // Default to the FROM clause (first target)
            default -> true;
        };
    }

    protected final @Nullable SelectTarget findSelectTargetOrNull(final ExpressionSpec expressionSpec, final List<SelectTarget> selectTargets) {
        return switch (expressionSpec) {
            case QueryField queryField -> {
                final Class<?> dtoClass = QueryFieldInspector.getDtoClass(queryField);
                final OrmTable ormTable = tableRegistry.getOrmTable(dtoClass);

                if (ormTable != null) {
                    final Table table = ormTable.getMetaData().table();
                    yield selectTargets.stream()
                            .filter(selectTarget -> table.equals(getTable(selectTarget)))
                            .findFirst()
                            .orElse(null);
                } else {
                    yield selectTargets.stream()
                            .filter(selectTarget -> {
                                final Table table = getTable(selectTarget);
                                final OrmTable stOrm = tableRegistry.getOrmTable(table);
                                return stOrm != null && dtoClass.equals(stOrm.dtoClass());
                            })
                            .findFirst()
                            .orElse(null);
                }
            }
            case ColumnExpressionSpec columnExpressionSpec -> {
                final String tableAlias = columnExpressionSpec.getTableAlias();

                if (tableAlias == null) {
                    yield findSelectTargetOrNull(columnExpressionSpec.getColumn(), selectTargets);
                }

                yield selectTargets.stream()
                        .filter(selectTarget -> tableAlias.equals(getAlias(selectTarget)))
                        .findFirst()
                        .orElse(null);
            }
            case Resolvable resolvable -> findSelectTarget(resolvable.column(), selectTargets);
            // Default to the FROM clause (first target)
            default -> selectTargets.getFirst();
        };
    }

    protected final SelectTarget findSelectTarget(final ExpressionSpec expressionSpec, final List<SelectTarget> selectTargets) {
        final SelectTarget selectTarget = findSelectTargetOrNull(expressionSpec, selectTargets);

        if (selectTarget == null) {
            throw new IllegalArgumentException("Could not find select target for expression spec " + expressionSpec);
        }

        return selectTarget;
    }

    protected final @Nullable SelectTarget findSelectTargetOrNull(final Column column, final List<SelectTarget> selectTargets) {
        final Table table = column.table();
        return selectTargets.stream()
                .filter(selectTarget -> table.equals(getTable(selectTarget)))
                .min(Comparator.comparing(this::getAlias,
                        Comparator.nullsFirst(Comparator.naturalOrder())))
                .orElse(null);
    }

    protected final SelectTarget findSelectTarget(final String columnName, final List<SelectTarget> selectTargets) {
        if (columnName.isEmpty()) {
            // Aggregate function or similar (e.g. COUNT(*)); return the "FROM" target (first)
            return selectTargets.getFirst();
        }

        SelectTarget selectTarget = null;

        for (SelectTarget st : selectTargets) {
            final Table table = getTable(st);

            if (table.isVirtual()) {
                // Fallback on the last seen virtual table if no real table match was found for this column
                selectTarget = st;
                continue;
            }

            if (matchesSelectTarget(columnName, st)) {
                selectTarget = st;
                break;
            }
        }

        if (selectTarget == null) {
            throw new IllegalArgumentException("No such column: " + columnName);
        }

        return selectTarget;
    }

    protected final boolean matchesSelectTarget(final String columnName, final SelectTarget selectTarget) {
        if (columnName.isEmpty()) {
            // Aggregate function or similar (e.g. COUNT(*)); return the "FROM" target (first)
            return true;
        }

        final Table table = getTable(selectTarget);

        if (table.isVirtual()) {
            return true;
        }

        if (litebridgeContext.mode() == LitebridgeContext.Mode.DTO) {
            final OrmTable ormTable = tableRegistry.getOrmTableOrThrow(table);
            return ormTable.hasField(columnName);
        } else {
            final TableMetaDataCache tableMetaDataCache = litebridgeContext.tableMetaDataCache();
            final TableMetaData tableMetaData = tableMetaDataCache.ensureTableMetaData(table);
            return tableMetaData.hasColumn(columnName);
        }
    }

    private ExpressionSpec resolveConditionExpressionSpec(final ExpressionSpec expressionSpec,
                                                          final List<SelectTarget> selectTargets,
                                                          final Operator operator) {
        if (expressionSpec instanceof ProtoExpressionSpec || expressionSpec instanceof QueryField) {
            final TargetResolution resolution = resolveSelectTarget(expressionSpec, selectTargets, operator);
            final List<ExpressionSpec> resolved = litebridgeContext.selectExpressionMapper()
                    .resolveProtoExpression(expressionSpec, resolution.ormTable(), resolution.table(), resolution.tableAlias(), ClauseType.WHERE);

            if (resolved.size() != 1) {
                throw new IllegalArgumentException("Expected exactly one expression spec, but got " + resolved.size());
            }

            return resolved.getFirst();
        }

        return expressionSpec;
    }

    private record TargetResolution(SelectTarget selectTarget,
                                    Table table,
                                    @Nullable OrmTable ormTable,
                                    @Nullable String tableAlias) {
    }

    protected record SelectExpressions(List<SelectExpression> expressions, Map<String, SelectExpression> aliases) {
    }
}
