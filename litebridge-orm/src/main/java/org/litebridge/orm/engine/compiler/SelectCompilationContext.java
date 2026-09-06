package org.litebridge.orm.engine.compiler;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.MappedFieldTarget;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.expression.ColumnExpression;
import org.litebridge.db.spi.expression.ConvertExpression;
import org.litebridge.db.spi.expression.SelectExpression;
import org.litebridge.db.spi.expression.SqlFunctionRegistry;
import org.litebridge.db.spi.query.ConditionGroup;
import org.litebridge.db.spi.query.Join;
import org.litebridge.db.spi.query.Limit;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.db.spi.query.OrderBy;
import org.litebridge.db.spi.query.Select;
import org.litebridge.orm.api.select.model.SelectExpressionMapper;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.ConditionJoinUsingNode;
import org.litebridge.orm.engine.ast.ConditionNode;
import org.litebridge.orm.engine.ast.ConditionWithIdNode;
import org.litebridge.orm.engine.ast.GroupByNode;
import org.litebridge.orm.engine.ast.JoinNode;
import org.litebridge.orm.engine.ast.LimitNode;
import org.litebridge.orm.engine.ast.OrderByNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.ast.SelectNode;
import org.litebridge.orm.expression.ColumnExpressionSpec;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.intent.ConvertSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;
import org.litebridge.orm.expression.select.SelectFieldSpec;
import org.litebridge.orm.persistence.MappedManyToMany;
import org.litebridge.orm.persistence.MappedOneToMany;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.TableRegistry;
import org.litebridge.orm.persistence.alias.AliasGenerator;
import org.litebridge.tracking.FieldAccessor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

final class SelectCompilationContext extends AbstractCompilationContext {

    private final boolean selectAll;
    private final Table aliasedTable;
    private final Map<String, Table> aliasedTables = new HashMap<>();
    private final TableMetaData tableMetaData;
    private final @Nullable OrmTable ormTable;
    private final List<SelectExpression> selectExpressions;
    private final TableRegistry tableRegistry;
    private final SelectExpressionMapper selectExpressionMapper;
    private final AliasGenerator aliasGenerator;
    private final Map<QueryNode, OrmTable> nodeOrmTableMap;
    private final Map<QueryNode, Table> nodeAliasedTableMap = new HashMap<>();
    private @Nullable List<JoinSpec> joinSpecs;
    private @Nullable JoinSpec currentJoinSpec;
    private @Nullable ConditionGroupSpecStack where;
    private @Nullable List<SelectExpression> groupBy;
    private @Nullable ConditionGroupSpecStack having;
    private @Nullable List<OrderBy> orderBys;
    private @Nullable Limit limit;

    SelectCompilationContext(final SelectNode selectNode,
                             final LitebridgeContext litebridgeContext) {
        super(litebridgeContext);
        this.selectExpressionMapper = litebridgeContext.selectExpressionMapper();
        this.aliasGenerator = litebridgeContext.aliasGenerator();
        this.selectAll = selectNode.isSelectAll();
        tableRegistry = litebridgeContext.tableRegistry();

        if (selectNode.dtoClass() != null) {
            if (selectNode.contextDtoClass() != null) {
                this.ormTable = tableRegistry.getTableInContextOrThrow(selectNode.dtoClass(), selectNode.contextDtoClass());
            } else {
                this.ormTable = tableRegistry.getOrmTableOrThrow(selectNode.dtoClass());
            }

            this.tableMetaData = ormTable.getMetaData();
            this.aliasedTable = aliasTable(tableMetaData.toTable());
            this.nodeOrmTableMap = new HashMap<>();
            this.nodeOrmTableMap.put(selectNode, ormTable);
            this.nodeAliasedTableMap.put(selectNode, aliasedTable);
        } else {
            this.aliasedTable = aliasTable(tableRegistry.getOrCreateSpiTable(Objects.requireNonNull(selectNode.table())));
            this.tableMetaData = litebridgeContext.tableMetaDataCache().ensureTableMetaData(aliasedTable);
            this.ormTable = null;
            this.nodeOrmTableMap = null;
            this.nodeAliasedTableMap.put(selectNode, aliasedTable);
        }

        final SqlFunctionRegistry sqlFunctionRegistry = litebridgeContext.sqlFunctionRegistry();

        if (selectAll) {
            final List<ColumnMetaData> columnMetaDatas;

            if (ormTable != null) {
                // All mapped columns
                columnMetaDatas = ormTable.mappedColumns();
            } else {
                // All columns
                columnMetaDatas = tableMetaData.columns();
            }

            this.selectExpressions = new ArrayList<>(columnMetaDatas.size());

            for (ColumnMetaData columnMetaData : columnMetaDatas) {
                final Column aliasedColumn = aliasGenerator.aliasColumn(aliasedTable, columnMetaData);
                this.selectExpressions.add(sqlFunctionRegistry.select().column().create(aliasedColumn));
            }
        } else if (selectNode.columns() != null) {
            // Specific field/column names specified
            this.selectExpressions = new ArrayList<>(selectNode.columns().length);

            if (ormTable != null) {
                // Translate field names to column names
                for (final String fieldName : selectNode.columns()) {
                    final ColumnMetaData columnMetaData = ormTable.columnMetaDataForField(fieldName);
                    final Column aliasedColumn = aliasGenerator.aliasColumn(aliasedTable, columnMetaData);
                    this.selectExpressions.add(sqlFunctionRegistry.select().column().create(aliasedColumn));
                }
            } else {
                for (final String columnName : selectNode.columns()) {
                    final ColumnMetaData columnMetaData = tableMetaData.column(columnName);
                    final Column aliasedColumn = aliasGenerator.aliasColumn(aliasedTable, columnMetaData);
                    this.selectExpressions.add(sqlFunctionRegistry.select().column().create(aliasedColumn));
                }
            }
        } else {
            // Select expressions
            final ExpressionSpec[] expressionSpecs = Objects.requireNonNull(selectNode.expressions());
            final List<ExpressionSpec> resolvedExpressionSpecs = new ArrayList<>(expressionSpecs.length);

            for (ExpressionSpec expressionSpec : expressionSpecs) {
                resolvedExpressionSpecs.addAll(selectExpressionMapper.resolveProtoExpression(expressionSpec, ormTable, aliasedTable, ClauseType.SELECT));
            }

            this.selectExpressions = resolvedExpressionSpecs.stream()
                    .map(this::aliasExpression)
                    .map(expressionSpec -> selectExpressionMapper.toSelectExpression(expressionSpec, false))
                    .toList();
        }
    }

    private static @Nullable ColumnExpressionSpec findColumnExpressionSpec(final ExpressionSpec expressionSpec) {
        final ExpressionSpec targetExpressionSpec;

        if (expressionSpec instanceof ConvertSpec<?> convertSpec) {
            targetExpressionSpec = convertSpec.target();
        } else {
            targetExpressionSpec = expressionSpec;
        }

        if (targetExpressionSpec instanceof ColumnExpressionSpec columnExpressionSpec) {
            return columnExpressionSpec;
        } else {
            return null;
        }
    }

    private static @Nullable Column findColumn(final SelectExpression selectExpression) {
        final SelectExpression targetExpression;

        if (selectExpression instanceof ConvertExpression convertExpression) {
            targetExpression = convertExpression.target();
        } else {
            targetExpression = selectExpression;
        }

        if (targetExpression instanceof ColumnExpression columnExpression) {
            return columnExpression.column();
        } else {
            return null;
        }
    }

    public void addJoin(final JoinNode joinNode) {
        final OrmTable targetOrmTable;

        if (joinNode.dtoClass() != null) {
            final Class<?> joinNodeDtoClass = joinNode.dtoClass();
            final QueryNode previousNode = joinNode.previous();
            final OrmTable sourceOrmTable = nodeOrmTableMap.get(previousNode);

            if (sourceOrmTable != null) {
                final OrmTable contextOrmTable = sourceOrmTable.getContextTableRegistry().getOrmTable(joinNodeDtoClass);
                targetOrmTable = contextOrmTable != null ? contextOrmTable : tableRegistry.getOrmTableOrThrow(joinNodeDtoClass);
            } else {
                targetOrmTable = tableRegistry.getOrmTableOrThrow(joinNodeDtoClass);
            }

            nodeOrmTableMap.put(joinNode, targetOrmTable);
        } else {
            targetOrmTable = null;
        }

        addJoinImpl(joinNode.type(), joinNode.dtoClass(), joinNode.rightTable(), targetOrmTable, joinNode);
    }

    private JoinSpec addJoinImpl(final String type,
                                 final @Nullable Class<?> dtoClass,
                                 final @Nullable String rightTable,
                                 final @Nullable OrmTable targetOrmTable,
                                 final JoinNode joinNode) {
        final JoinSpec joinSpec = new JoinSpec(type, dtoClass, rightTable, targetOrmTable, joinNode);
        joinSpec.conditionGroupStack().newRootInstance();

        if (this.joinSpecs == null) {
            this.joinSpecs = new ArrayList<>();
        }

        this.joinSpecs.add(joinSpec);
        currentJoinSpec = joinSpec;
        return joinSpec;
    }

    public ConditionGroupSpecStack joinConditionGroupStack() {
        return Objects.requireNonNull(currentJoinSpec, "No current JOIN")
                .conditionGroupStack();
    }

    public void addJoinCondition(final ConditionNode conditionNode) {
        final JoinSpec joinSpec = Objects.requireNonNull(currentJoinSpec, "No current JOIN");
        final Object rhs;

        if (conditionNode.rhsColumn() != null) {
            final ColumnMetaData relationshipColumn = ormTable.columnMetaDataForField(conditionNode.rhsColumn());
            final FieldAccessor relationshipFieldAccessor = ormTable.getFieldForColumnName(relationshipColumn.name());
            rhs = new SelectFieldSpec(relationshipFieldAccessor, relationshipColumn.toColumn());
        } else {
            rhs = conditionNode.rhs();
        }

        joinSpec
                .conditionGroupStack().current()
                .newCondition(conditionNode.logicOperator(),
                        conditionNode.lhsColumn(),
                        conditionNode.lhsExpression(),
                        conditionNode.operator(),
                        rhs);
    }

    public void addWhereCondition(final ConditionNode conditionNode) {
        ensureWhereConditionGroupStack().current()
                .newCondition(conditionNode.logicOperator(),
                        conditionNode.lhsColumn(),
                        conditionNode.lhsExpression(),
                        conditionNode.operator(),
                        conditionNode.rhs());
    }

    public ConditionNode toConditionNode(final ConditionWithIdNode conditionWithIdNode) {
        final String[] primaryKeyFieldNames = tableMetaData.primaryKey().stream()
                .map(columnMetaData -> ormTable.getFieldForColumnName(columnMetaData.name()).name())
                .toArray(String[]::new);

        final Object id = conditionWithIdNode.id();
        ConditionNode conditionNode = null;

        if (primaryKeyFieldNames.length == 0) {
            throw new IllegalArgumentException("No primary key fields found for table " + tableMetaData.name());
        } else if (primaryKeyFieldNames.length == 1) {
            conditionNode = new ConditionNode(null, conditionWithIdNode.logicOperator(), primaryKeyFieldNames[0], null, conditionWithIdNode.operator(), id);
        } else {
            // Composite PK
            switch (id) {
                case List<?> idList -> {
                    if (idList.size() != primaryKeyFieldNames.length) {
                        throw new IllegalArgumentException("Invalid number of primary key values for table %s; expected: %d, actual: %d".formatted(ormTable.getMetaData().name(), primaryKeyFieldNames.length, idList.size()));
                    }

                    for (int i = 0; i < primaryKeyFieldNames.length; i++) {
                        final LogicOperator logicOperator = conditionNode == null ? LogicOperator.NOOP : LogicOperator.AND;
                        conditionNode = new ConditionNode(conditionNode, logicOperator, primaryKeyFieldNames[i], null, conditionWithIdNode.operator(), idList.get(i));
                    }
                }
                case Object[] idArray -> {
                    if (idArray.length != primaryKeyFieldNames.length) {
                        throw new IllegalArgumentException("Invalid number of primary key values for table %s; expected: %d, actual: %d".formatted(ormTable.getMetaData().name(), primaryKeyFieldNames.length, idArray.length));
                    }

                    for (int i = 1; i < primaryKeyFieldNames.length; i++) {
                        conditionNode = new ConditionNode(conditionNode, conditionWithIdNode.logicOperator(), primaryKeyFieldNames[i], null, conditionWithIdNode.operator(), idArray[i]);
                    }
                }
                case Map<?, ?> idMap -> {
                    if (idMap.size() != primaryKeyFieldNames.length) {
                        throw new IllegalArgumentException("Invalid number of primary key values for table %s; expected: %d, actual: %d".formatted(ormTable.getMetaData().name(), primaryKeyFieldNames.length, idMap.size()));
                    }

                    for (int i = 0; i < primaryKeyFieldNames.length; i++) {
                        conditionNode = new ConditionNode(conditionNode, conditionWithIdNode.logicOperator(), primaryKeyFieldNames[0], null, conditionWithIdNode.operator(), idMap.get(primaryKeyFieldNames[i]));
                    }
                }
                case null, default ->
                        throw new IllegalArgumentException("Invalid composite primary key value type provided; expected: List<?>, Object[], or Map<String, ?>");
            }
        }

        return Objects.requireNonNull(conditionNode, "Condition node not resolved for 'withId' condition");
    }

    public void addJoinCondition(final ConditionJoinUsingNode conditionJoinUsingNode) {
        final String fieldName = Objects.requireNonNull(conditionJoinUsingNode.usingColumn(), "Using column not provided");

        JoinSpec joinSpec = Objects.requireNonNull(currentJoinSpec, "No current JOIN");
        final QueryNode sourceNode = findSourceNodeForField(joinSpec.joinNode(), fieldName);
        final OrmTable activeSourceOrmTable = nodeOrmTableMap.get(sourceNode);
        final Table activeSourceAliasedTable = nodeAliasedTableMap.get(sourceNode);

        // Get details on the USING column on the local table
        final MappedFieldTarget mappedFieldTarget = activeSourceOrmTable.mappedFieldTargetForField(fieldName);
        joinSpec = Objects.requireNonNull(currentJoinSpec, "No current JOIN");
        final Class<?> joinDtoClass = Objects.requireNonNull(joinSpec.dtoClass(), "No DTO class specified for join");

        switch (mappedFieldTarget) {
            case ColumnMetaData usingColumnMetaData -> {
                final JoinOnSpec joinOnSpec = processOneToManyJoin(joinDtoClass, usingColumnMetaData, activeSourceAliasedTable);
                final ConditionNode conditionNode = new ConditionNode(null, conditionJoinUsingNode.logicOperator(), null, joinOnSpec.leftSelectColumnSpec(), Operator.EQ, joinOnSpec.rightSelectColumnSpec());
                joinSpec.setAliasedTable(joinOnSpec.rightSelectColumnSpec().getColumn().table());
                nodeAliasedTableMap.put(joinSpec.joinNode(), joinSpec.getAliasedTable());
                addJoinCondition(conditionNode);
            }
            case MappedOneToMany mappedOneToMany -> {
                final JoinOnSpec joinOnSpec = processOneToManyReverseJoin(joinDtoClass, mappedOneToMany, activeSourceAliasedTable);
                final ConditionNode conditionNode = new ConditionNode(null, conditionJoinUsingNode.logicOperator(), null, joinOnSpec.leftSelectColumnSpec(), Operator.EQ, joinOnSpec.rightSelectColumnSpec());
                joinSpec.setAliasedTable(joinOnSpec.rightSelectColumnSpec().getColumn().table());
                nodeAliasedTableMap.put(joinSpec.joinNode(), joinSpec.getAliasedTable());
                addJoinCondition(conditionNode);
            }
            case MappedManyToMany mappedManyToMany -> {
                final List<JoinOnSpec> joinOnSpecs = processManyToManyJoin(mappedManyToMany, activeSourceAliasedTable);

                // First join
                final JoinOnSpec firstJoinOnSpec = joinOnSpecs.getFirst();
                final ConditionNode firstConditionNode = new ConditionNode(null, conditionJoinUsingNode.logicOperator(), null, firstJoinOnSpec.leftSelectColumnSpec(), Operator.EQ, firstJoinOnSpec.rightSelectColumnSpec());
                joinSpec.setAliasedTable(firstJoinOnSpec.rightSelectColumnSpec().getColumn().table());
                // Note: the nodeAliasedTableMap update here is for the join table, which might not be what we want for nested joins
                addJoinCondition(firstConditionNode);

                // Second join
                joinSpec = addJoinImpl("INNER", mappedManyToMany.targetOrmTable().get().dtoClass(), null, mappedManyToMany.targetOrmTable().get(), joinSpec.joinNode());
                final JoinOnSpec secondJoinOnSpec = joinOnSpecs.getLast();
                final ConditionNode secondConditionNode = new ConditionNode(null, conditionJoinUsingNode.logicOperator(), null, secondJoinOnSpec.leftSelectColumnSpec(), Operator.EQ, secondJoinOnSpec.rightSelectColumnSpec());
                joinSpec.setAliasedTable(secondJoinOnSpec.rightSelectColumnSpec().getColumn().table());
                nodeAliasedTableMap.put(joinSpec.joinNode(), joinSpec.getAliasedTable());
                addJoinCondition(secondConditionNode);
            }
            default -> throw new UnsupportedOperationException("Unsupported mapped field target: " + mappedFieldTarget);
        }
    }

    public ConditionGroupSpecStack ensureWhereConditionGroupStack() {
        if (where == null) {
            where = new ConditionGroupSpecStack();
        }

        return where;
    }

    public void addGroupBy(final GroupByNode groupByNode) {
        final List<SelectExpression> groupByExpressions;

        if (groupByNode.expressions() != null) {
            // Explicit expression
            groupByExpressions = Arrays.stream(groupByNode.expressions())
                    .flatMap(expressionSpec -> selectExpressionMapper
                            .resolveProtoExpression(expressionSpec, ormTable, aliasedTable, ClauseType.GROUP_BY)
                            .stream())
                    .map(this::resolveAlias)
                    .map(expressionSpec -> selectExpressionMapper.toSelectExpression(expressionSpec, true))
                    .toList();
        } else {
            // Column/field names
            final String[] columnNames = Objects.requireNonNull(groupByNode.columns());
            final SqlFunctionRegistry sqlFunctionRegistry = litebridgeContext.sqlFunctionRegistry();

            if (ormTable != null) {
                // DTO field names; translate to columns
                groupByExpressions = Arrays.stream(columnNames)
                        .map(columnName -> {
                            final ColumnMetaData columnMetaData = ormTable.columnMetaDataForField(columnName);
                            final Column aliasedColumn = resolveAlias(aliasedTable, columnMetaData);
                            return (SelectExpression) sqlFunctionRegistry.select().reference().create(aliasedColumn);
                        })
                        .toList();
            } else {
                // Column names
                groupByExpressions = Arrays.stream(columnNames)
                        .map(columnName -> {
                            final Column column = resolveAlias(aliasedTable, columnName);
                            return (SelectExpression) sqlFunctionRegistry.select().reference().create(column);
                        })
                        .toList();
            }
        }

        if (groupBy == null) {
            groupBy = new ArrayList<>();
        }

        groupBy.addAll(groupByExpressions);
    }

    public void addHavingCondition(final ConditionNode conditionNode) {
        ensureHavingConditionGroupStack().current()
                .newCondition(conditionNode.logicOperator(),
                        conditionNode.lhsColumn(),
                        conditionNode.lhsExpression(),
                        conditionNode.operator(),
                        conditionNode.rhs());
    }

    public ConditionGroupSpecStack ensureHavingConditionGroupStack() {
        if (having == null) {
            having = new ConditionGroupSpecStack();
        }

        return having;
    }

    public void addOrderBy(final OrderByNode orderByNode) {
        final List<SelectExpression> orderByExpressions;

        if (orderByNode.expression() != null) {
            // Explicit expression
            orderByExpressions = selectExpressionMapper.resolveProtoExpression(orderByNode.expression(), ormTable, aliasedTable, ClauseType.ORDER_BY).stream()
                    .map(this::resolveAlias)
                    .map(expressionSpec -> selectExpressionMapper.toSelectExpression(expressionSpec, true))
                    .toList();
        } else {
            // Column/field names
            final String columnName = Objects.requireNonNull(orderByNode.column());
            final Column aliasedColumn;

            if (ormTable != null) {
                // DTO field name; translate it to a column
                final ColumnMetaData columnMetaData = ormTable.columnMetaDataForField(columnName);
                aliasedColumn = resolveAlias(aliasedTable, columnMetaData);
            } else {
                // Column name
                aliasedColumn = resolveAlias(aliasedTable, columnName);
            }

            orderByExpressions = Collections.singletonList(litebridgeContext.sqlFunctionRegistry().select().reference().create(aliasedColumn));
        }

        if (orderBys == null) {
            orderBys = new ArrayList<>();
        }

        for (final SelectExpression orderByExpression : orderByExpressions) {
            final OrderBy orderBy = new OrderBy(orderByExpression, orderByNode.ascending());
            orderBys.add(orderBy);
        }
    }

    public void setLimit(final LimitNode limitNode) {
        this.limit = new Limit(limitNode.limit(), limitNode.offset());
    }

    @Override
    public Select toOperation() {
        final List<Join> joins;

        if (joinSpecs != null) {
            joins = joinSpecs.stream()
                    .map(joinSpec -> {
                        final Table joinTable;

                        if (joinSpec.getAliasedTable() != null) {
                            joinTable = joinSpec.getAliasedTable();
                        } else if (joinSpec.dtoClass() != null) {
                            joinTable = tableRegistry.getOrmTableOrThrow(joinSpec.dtoClass()).getMetaData().toTable();
                        } else {
                            joinTable = tableRegistry.getOrCreateSpiTable(Objects.requireNonNull(joinSpec.tableName()));
                        }

                        final ConditionGroup joinConditionGroup = toConditionGroup(joinSpec.conditionGroupStack().current(), ormTable, aliasedTable);
                        return new Join(joinTable, joinConditionGroup);
                    })
                    .toList();
        } else {
            joins = null;
        }

        final ConditionGroup whereConditionGroup = where != null ? toConditionGroup(where.current(), ormTable, aliasedTable) : null;
        final ConditionGroup havingConditionGroup = having != null ? toConditionGroup(having.current(), ormTable, aliasedTable) : null;

        return new Select(aliasedTable,
                selectExpressions,
                joins,
                whereConditionGroup,
                groupBy,
                havingConditionGroup,
                orderBys,
                limit);
    }

    @Override
    protected Column resolveAlias(final Table table, final ColumnMetaData columnMetaData) {
        return resolveAlias(table, columnMetaData.name(), columnMetaData::toColumn);
    }

    @Override
    protected Column resolveAlias(final Table table, final Column column) {
        return resolveAlias(table, column.name(), () -> column);
    }

    @Override
    protected ExpressionSpec resolveAlias(final ExpressionSpec expressionSpec) {
        final ColumnExpressionSpec columnExpressionSpec = findColumnExpressionSpec(expressionSpec);

        if (columnExpressionSpec != null) {
            final Column column = columnExpressionSpec.getColumn();
            final Column aliasedColumn = resolveAlias(column.table(), column);
            columnExpressionSpec.setColumn(aliasedColumn);
        }

        return expressionSpec;
    }

    private Table aliasTable(final Table table) {
        if (table == aliasedTable) {
            return table;
        } else if (table.equalsIgnoreAlias(aliasedTable)) {
            return aliasedTable;
        }

        return aliasedTables.computeIfAbsent(table.qualifiedName(), tableName -> aliasGenerator.aliasTable(table));
    }

    private Table aliasTable(final OrmTable ormTable) {
        return aliasTable(ormTable, false);
    }

    private Table aliasTable(final OrmTable ormTable, final boolean forceAlias) {
        if (forceAlias) {
            return aliasGenerator.aliasTable(ormTable);
        } else {
            final TableMetaData tableMetaData = ormTable.getMetaData();
            return aliasedTables.computeIfAbsent(tableMetaData.qualifiedName(), tableName -> aliasGenerator.aliasTable(ormTable));
        }
    }

    private Column resolveAlias(final Table table, final String columnName) {
        return resolveAlias(table, columnName, () -> new Column(table, columnName));
    }

    private Column resolveAlias(final Table table, final String columnName, final Supplier<Column> columnSupplier) {
        return selectExpressions.stream()
                .map(SelectCompilationContext::findColumn)
                .filter(Objects::nonNull)
                .filter(column -> table.equalsIgnoreAlias(column.table()) && columnName.equals(column.name()))
                .findFirst()
                .orElseGet(() -> {
                    // Column not in the select list; do not assign an alias to it, but use the alias of the table
                    final Column column = columnSupplier.get();
                    final Table aliasedTable = aliasedTables.get(table.qualifiedName());

                    if (aliasedTable != null) {
                        column.setTable(aliasedTable);
                    }

                    return column;
                });
    }

    private ExpressionSpec aliasExpression(final ExpressionSpec expressionSpec) {
        final ColumnExpressionSpec columnExpressionSpec = findColumnExpressionSpec(expressionSpec);

        if (columnExpressionSpec != null) {
            final Column column = columnExpressionSpec.getColumn();
            final Column aliasedColumn;

            if (column.table().equalsIgnoreAlias(aliasedTable)) {
                aliasedColumn = aliasGenerator.aliasColumn(aliasedTable, column);
            } else {
                //TODO: may need to alias the table itself
                aliasedColumn = aliasGenerator.aliasColumn(column.table(), column);
            }

            columnExpressionSpec.setColumn(aliasedColumn);
        }

        return expressionSpec;
    }

    private QueryNode findSourceNodeForField(final JoinNode joinNode, final String fieldName) {
        QueryNode current = joinNode.previous();

        while (current != null) {
            final OrmTable table = nodeOrmTableMap.get(current);

            if (table != null && table.mappedFieldTargetForFieldOrNull(fieldName) != null) {
                return current;
            }

            current = current.previous();
        }

        throw new IllegalStateException("No root node found for field name: " + fieldName);
    }

    private JoinOnSpec processOneToManyJoin(final Class<?> joinDtoClass, final ColumnMetaData leftColumnMetaData, final Table leftAliasedTable) {
        // Left column
        final SelectColumnSpec leftSelectColumnSpec = new SelectColumnSpec(resolveAlias(leftAliasedTable, leftColumnMetaData));

        // Right table & column
        final JoinSpec joinSpec = Objects.requireNonNull(currentJoinSpec, "No current JOIN");
        OrmTable rightOrmTable = joinSpec.ormTable();

        if (rightOrmTable == null) {
            rightOrmTable = tableRegistry.getOrmTable(Objects.requireNonNull(joinDtoClass));
        }

        final Table aliasedRightTable = aliasTable(rightOrmTable, true);
        final TableMetaData rightTableMetaData = rightOrmTable.getMetaData();
        final ColumnMetaData rightColumnMetaData = rightTableMetaData.column(leftColumnMetaData.getJoinColumn());

        // Add right table columns to select
        SelectColumnSpec rightSelectColumnSpec = null;

        if (selectAll) {
            final SqlFunctionRegistry sqlFunctionRegistry = litebridgeContext.sqlFunctionRegistry();

            for (ColumnMetaData columnMetaData : rightTableMetaData.columns()) {
                final Column aliasedColumn = aliasGenerator.aliasColumn(aliasedRightTable, columnMetaData);
                this.selectExpressions.add(sqlFunctionRegistry.select().column().create(aliasedColumn));

                if (columnMetaData.equals(rightColumnMetaData)) {
                    rightSelectColumnSpec = new SelectColumnSpec(aliasedColumn);
                }
            }
        }

        return new JoinOnSpec(leftSelectColumnSpec, Objects.requireNonNull(rightSelectColumnSpec, "Right JOIN column not selected"));
    }

    private List<JoinOnSpec> processManyToManyJoin(final MappedManyToMany mappedManyToMany, final Table leftAliasedTable) {
        final JoinOnSpec leftLeftJoinOnSpec = createManyToManyLeftJoinOnSpec(mappedManyToMany, leftAliasedTable);
        return List.of(leftLeftJoinOnSpec, createManyToManyRightJoinOnSpec(mappedManyToMany, leftLeftJoinOnSpec.rightSelectColumnSpec().getColumn().table()));
    }

    private @NonNull JoinOnSpec createManyToManyLeftJoinOnSpec(final MappedManyToMany mappedManyToMany, final Table leftAliasedTable) {
        // Left column
        final JoinSpec joinSpec = Objects.requireNonNull(currentJoinSpec, "No current JOIN");
        final OrmTable leftOrmTable = nodeOrmTableMap.get(findSourceNodeForField(joinSpec.joinNode(), joinSpec.joinNode().condition() != null ? ((ConditionJoinUsingNode) joinSpec.joinNode().condition()).usingColumn() : ""));
        final TableMetaData leftTableMetaData = (leftOrmTable != null ? leftOrmTable : ormTable).getMetaData();

        if (leftTableMetaData.primaryKey().isEmpty()) {
            throw new IllegalArgumentException("Left table " + leftTableMetaData.name() + " does not have a primary key; cannot map many-to-many join: " + mappedManyToMany);
        }

        //TODO: add support for composite primary keys in many-to-many joins
        final ColumnMetaData leftColumnMetaData = leftTableMetaData.primaryKey().getFirst();
        final SelectColumnSpec leftSelectColumnSpec = new SelectColumnSpec(resolveAlias(leftAliasedTable, leftColumnMetaData));

        // Join table & column - alias it directly in order to support self-references
        final Table aliasedJoinTable = aliasGenerator.aliasTable(mappedManyToMany.joinOrmTable());
        final Column aliasedJoinColumn = resolveAlias(aliasedJoinTable, mappedManyToMany.joinColumn());
        final SelectColumnSpec joinSelectColumnSpec = new SelectColumnSpec(aliasedJoinColumn);

        return new JoinOnSpec(leftSelectColumnSpec, joinSelectColumnSpec);
    }

    private @NonNull JoinOnSpec createManyToManyRightJoinOnSpec(final MappedManyToMany mappedManyToMany, final Table aliasedJoinTable) {
        // Join table & column
        final SelectColumnSpec joinSelectColumnSpec = new SelectColumnSpec(resolveAlias(aliasedJoinTable, mappedManyToMany.inverseJoinColumn()));

        // Right column
        final OrmTable rightOrmTable = mappedManyToMany.targetOrmTable().get();
        final TableMetaData rightTableMetaData = rightOrmTable.getMetaData();

        if (rightTableMetaData.primaryKey().isEmpty()) {
            throw new IllegalArgumentException("Right table " + tableMetaData.name() + " does not have a primary key; cannot map many-to-many join: " + mappedManyToMany);
        }

        //TODO: add support for composite primary keys in many-to-many joins
        final ColumnMetaData rightColumnMetaData = rightTableMetaData.primaryKey().getFirst();
        final Table aliasedRightTable = aliasTable(rightOrmTable);

        // Add joined table columns to select
        SelectColumnSpec rightSelectColumnSpec = null;

        if (selectAll) {
            final SqlFunctionRegistry sqlFunctionRegistry = litebridgeContext.sqlFunctionRegistry();

            for (ColumnMetaData columnMetaData : rightOrmTable.mappedColumns()) {
                final Column aliasedColumn = aliasGenerator.aliasColumn(aliasedRightTable, columnMetaData);
                this.selectExpressions.add(sqlFunctionRegistry.select().column().create(aliasedColumn));

                if (columnMetaData.equals(rightColumnMetaData)) {
                    rightSelectColumnSpec = new SelectColumnSpec(aliasedColumn);
                }
            }
        }

        return new JoinOnSpec(joinSelectColumnSpec, rightSelectColumnSpec);
    }

    private JoinOnSpec processOneToManyReverseJoin(final Class<?> joinDtoClass, final MappedOneToMany mappedOneToMany, final Table leftAliasedTable) {
        // Join table & column
        final JoinSpec joinSpec = Objects.requireNonNull(currentJoinSpec, "No current JOIN");
        OrmTable rightOrmTable = joinSpec.ormTable();

        if (rightOrmTable == null) {
            rightOrmTable = litebridgeContext.tableRegistry().getOrmTableOrThrow(Objects.requireNonNull(joinDtoClass));
        }

        final Table aliasedRightTable = aliasTable(rightOrmTable);
        final ColumnMetaData rightColumnMetaData = rightOrmTable.columnMetaDataForField(mappedOneToMany.mappedByField());

        // Local column
        //TODO: composite primary keys
        final OrmTable leftOrmTable = nodeOrmTableMap.get(findSourceNodeForField(joinSpec.joinNode(), joinSpec.joinNode().condition() != null ? ((ConditionJoinUsingNode) joinSpec.joinNode().condition()).usingColumn() : "")); // Rough but okay for reverse
        final ColumnMetaData leftColumnMetaData = (leftOrmTable != null ? leftOrmTable : ormTable).getMetaData().primaryKey().getFirst();
        final Column leftAliasedColumn = resolveAlias(leftAliasedTable, leftColumnMetaData);
        final SelectColumnSpec leftSelectColumnSpec = new SelectColumnSpec(leftAliasedColumn);

        // Add join table columns to select
        SelectColumnSpec rightSelectColumnSpec = null;

        if (selectAll) {
            final SqlFunctionRegistry sqlFunctionRegistry = litebridgeContext.sqlFunctionRegistry();

            for (ColumnMetaData columnMetaData : rightOrmTable.getMetaData().columns()) {
                final Column aliasedColumn = aliasGenerator.aliasColumn(aliasedRightTable, columnMetaData);
                this.selectExpressions.add(sqlFunctionRegistry.select().column().create(aliasedColumn));

                if (columnMetaData.equals(rightColumnMetaData)) {
                    rightSelectColumnSpec = new SelectColumnSpec(aliasedColumn);
                }
            }
        }

        return new JoinOnSpec(leftSelectColumnSpec, Objects.requireNonNull(rightSelectColumnSpec, "Right JOIN column not selected"));
    }

    private record JoinOnSpec(SelectColumnSpec leftSelectColumnSpec, SelectColumnSpec rightSelectColumnSpec) {
    }
}
