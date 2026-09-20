//package org.litebridge.orm.engine.compiler;
//
//import org.jspecify.annotations.NonNull;
//import org.jspecify.annotations.Nullable;
//import org.litebridge.db.spi.Column;
//import org.litebridge.db.spi.ColumnMetaData;
//import org.litebridge.db.spi.MappedFieldTarget;
//import org.litebridge.db.spi.PreparedOperation;
//import org.litebridge.db.spi.Table;
//import org.litebridge.db.spi.TableMetaData;
//import org.litebridge.db.spi.alias.AliasedQuery;
//import org.litebridge.db.spi.alias.AliasedTable;
//import org.litebridge.db.spi.expression.ClauseType;
//import org.litebridge.db.spi.expression.ColumnExpression;
//import org.litebridge.db.spi.expression.ConvertExpression;
//import org.litebridge.db.spi.expression.SelectExpression;
//import org.litebridge.db.spi.expression.SqlFunctionRegistry;
//import org.litebridge.db.spi.query.ConditionGroup;
//import org.litebridge.db.spi.query.Join;
//import org.litebridge.db.spi.query.Limit;
//import org.litebridge.db.spi.query.LogicOperator;
//import org.litebridge.db.spi.query.Operator;
//import org.litebridge.db.spi.query.OrderBy;
//import org.litebridge.db.spi.query.Select;
//import org.litebridge.db.spi.query.SelectTarget;
//import org.litebridge.orm.api.select.model.SelectExpressionMapper;
//import org.litebridge.orm.engine.LitebridgeContext;
//import org.litebridge.orm.engine.ast.ConditionJoinUsingNode;
//import org.litebridge.orm.engine.ast.ConditionNode;
//import org.litebridge.orm.engine.ast.ConditionWithIdNode;
//import org.litebridge.orm.engine.ast.GroupByNode;
//import org.litebridge.orm.engine.ast.HavingNode;
//import org.litebridge.orm.engine.ast.JoinNode;
//import org.litebridge.orm.engine.ast.LimitNode;
//import org.litebridge.orm.engine.ast.OrderByNode;
//import org.litebridge.orm.engine.ast.QueryNode;
//import org.litebridge.orm.engine.ast.SelectNode;
//import org.litebridge.orm.engine.ast.WhereNode;
//import org.litebridge.orm.expression.ColumnExpressionSpec;
//import org.litebridge.orm.expression.ExpressionSpec;
//import org.litebridge.orm.expression.intent.ConvertSpec;
//import org.litebridge.orm.expression.select.SelectColumnSpec;
//import org.litebridge.orm.expression.select.SelectFieldSpec;
//import org.litebridge.orm.persistence.MappedManyToMany;
//import org.litebridge.orm.persistence.MappedOneToMany;
//import org.litebridge.orm.persistence.OrmTable;
//import org.litebridge.orm.persistence.TableRegistry;
//import org.litebridge.orm.persistence.alias.AliasGenerator;
//import org.litebridge.tracking.FieldAccessor;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.IdentityHashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Objects;
//import java.util.function.Supplier;
//import java.util.stream.Stream;
//
///**
// * Compilation context for SELECT statements.
// */
//final class SelectCompilationContextOld extends AbstractCompilationContext {
//
//    private final boolean selectAll;
//    private final Table table;
//    private final Map<Table, String> tableAliases = new IdentityHashMap<>();
//    private final TableMetaData tableMetaData;
//    private final @Nullable OrmTable ormTable;
//    private final @Nullable QueryNode fromQueryNode;
//    private final List<SelectExpression> selectExpressions;
//    private final TableRegistry tableRegistry;
//    private final SelectExpressionMapper selectExpressionMapper;
//    private final AliasGenerator aliasGenerator;
//    private final Map<QueryNode, OrmTable> nodeOrmTableMap;
//    private final Map<QueryNode, Table> nodeAliasedTableMap = new HashMap<>();
//    private @Nullable String fromAlias;
//    private @Nullable List<JoinSpec> joinSpecs;
//    private @Nullable JoinSpec currentJoinSpec;
//    private @Nullable ConditionGroupSpecStack where;
//    private @Nullable List<SelectExpression> groupBy;
//    private @Nullable ConditionGroupSpecStack having;
//    private @Nullable List<OrderBy> orderBys;
//    private @Nullable Limit limit;
//
//    SelectCompilationContext(final SelectNode selectNode,
//                             final LitebridgeContext litebridgeContext) {
//        super(litebridgeContext);
//        this.selectExpressionMapper = litebridgeContext.selectExpressionMapper();
//        this.aliasGenerator = litebridgeContext.aliasGenerator();
//        this.selectAll = selectNode.isSelectAll();
//        this.tableRegistry = litebridgeContext.tableRegistry();
//        this.fromAlias = selectNode.alias();
//
//        if (selectNode.dtoClass() != null) {
//            if (selectNode.contextDtoClass() != null) {
//                this.ormTable = tableRegistry.getOrmTableInContextOrThrow(selectNode.dtoClass(), selectNode.contextDtoClass());
//            } else {
//                this.ormTable = tableRegistry.getOrmTableOrThrow(selectNode.dtoClass());
//            }
//
//            this.tableMetaData = ormTable.getMetaData();
//            this.table = aliasTable(tableMetaData.table());
//            this.nodeOrmTableMap = new HashMap<>();
//            this.nodeOrmTableMap.put(selectNode, ormTable);
//            this.nodeAliasedTableMap.put(selectNode, table);
//            this.fromQueryNode = null;
//            this.fromAlias = aliasGenerator.newTableAlias(table);
//        } else if (selectNode.table() != null) {
//            this.table = aliasTable(tableRegistry.getOrCreateSpiTable(selectNode.table()));
//            this.tableMetaData = litebridgeContext.tableMetaDataCache().ensureTableMetaData(table);
//            this.ormTable = null;
//            this.nodeOrmTableMap = null;
//            this.nodeAliasedTableMap.put(selectNode, table);
//            this.fromQueryNode = null;
//        } else {
//            //TODO: alias subquery
//            this.table = null;
//            //TODO: virtual metadata
//            this.tableMetaData = null;
//            this.fromQueryNode = Objects.requireNonNull(selectNode.fromQueryNode(), "No FROM table, DTO or subquery specified");
//            this.ormTable = null;
//            this.nodeOrmTableMap = null;
//        }
//
//        final SqlFunctionRegistry sqlFunctionRegistry = litebridgeContext.sqlFunctionRegistry();
//
//        if (selectAll) {
//            final List<ColumnMetaData> columnMetaDatas;
//
//            if (ormTable != null) {
//                // All mapped columns
//                columnMetaDatas = ormTable.mappedColumns();
//            } else {
//                // All columns; just select *, don't specify columns
//                columnMetaDatas = Collections.emptyList();
//            }
//
//            this.selectExpressions = new ArrayList<>(columnMetaDatas.size());
//
//            for (ColumnMetaData columnMetaData : columnMetaDatas) {
//                final Column column = columnMetaData.toColumn();
//                final String columnAlias;
//                final String tableAlias;
//
//                if (fromAlias != null) {
//                    columnAlias = aliasGenerator.newColumnAlias(column);
//                    tableAlias = aliasGenerator.newTableAlias(column.table());
//                } else {
//                    columnAlias = null;
//                    tableAlias = null;
//                }
//
//                this.selectExpressions.add(sqlFunctionRegistry.select().column().create(column, columnAlias, tableAlias));
//            }
//        } else if (selectNode.columns() != null) {
//            // Specific field/column names specified
//            this.selectExpressions = new ArrayList<>(selectNode.columns().length);
//
//            if (ormTable != null) {
//                // Translate field names to column names
//                for (final String fieldName : selectNode.columns()) {
//                    final ColumnMetaData columnMetaData = ormTable.columnMetaDataForField(fieldName);
//                    final Column column = columnMetaData.toColumn();
//                    final String columnAlias = aliasGenerator.newColumnAlias(column);
//                    final String tableAlias = aliasGenerator.newTableAlias(column.table());
//                    this.selectExpressions.add(sqlFunctionRegistry.select().column().create(column, columnAlias, tableAlias));
//                }
//            } else if (tableMetaData != null) {
//                for (final String columnName : selectNode.columns()) {
//                    final ColumnMetaData columnMetaData = tableMetaData.column(columnName);
//                    final Column column = columnMetaData.toColumn();
//                    final String tableAlias;
//
//                    if (fromAlias != null) {
//                        if (table.equals(column.table())) {
//                            tableAlias = fromAlias;
//                        } else {
//                            tableAlias = aliasGenerator.newTableAlias(column.table());
//                        }
//                    } else {
//                        tableAlias = null;
//                    }
//
//                    this.selectExpressions.add(sqlFunctionRegistry.select().column().create(column, null, tableAlias));
//                }
//            }
//        } else {
//            // Select expressions
//            final ExpressionSpec[] expressionSpecs = Objects.requireNonNull(selectNode.expressions());
//            final List<ExpressionSpec> resolvedExpressionSpecs = new ArrayList<>(expressionSpecs.length);
//
//            for (ExpressionSpec expressionSpec : expressionSpecs) {
//                resolvedExpressionSpecs.addAll(selectExpressionMapper.resolveProtoExpression(expressionSpec, ormTable, table, ClauseType.SELECT));
//            }
//
//            Stream<ExpressionSpec> expressionSpecStream = resolvedExpressionSpecs.stream();
//
//            if (litebridgeContext.mode() == LitebridgeContext.Mode.DTO) {
//                expressionSpecStream = expressionSpecStream.map(this::aliasExpression);
//            }
//
//            this.selectExpressions = expressionSpecStream.map(expressionSpec ->
//                            selectExpressionMapper.toSelectExpression(expressionSpec, false))
//                    .toList();
//        }
//    }
//
//    public ConditionGroupSpecStack setWhereNode(@Nullable final WhereNode whereNode) {
//        throw new UnsupportedOperationException();
//    }
//
//    public ConditionGroupSpecStack setHavingNode(final HavingNode havingNode) {
//        throw new UnsupportedOperationException();
//    }
//
//    private static @Nullable ColumnExpressionSpec findColumnExpressionSpec(final ExpressionSpec expressionSpec) {
//        final ExpressionSpec targetExpressionSpec;
//
//        if (expressionSpec instanceof ConvertSpec<?> convertSpec) {
//            targetExpressionSpec = convertSpec.target();
//        } else {
//            targetExpressionSpec = expressionSpec;
//        }
//
//        if (targetExpressionSpec instanceof ColumnExpressionSpec columnExpressionSpec) {
//            return columnExpressionSpec;
//        } else {
//            return null;
//        }
//    }
//
//    private static @Nullable Column findColumn(final SelectExpression selectExpression) {
//        final SelectExpression targetExpression;
//
//        if (selectExpression instanceof ConvertExpression convertExpression) {
//            targetExpression = convertExpression.target();
//        } else {
//            targetExpression = selectExpression;
//        }
//
//        if (targetExpression instanceof ColumnExpression columnExpression) {
//            return columnExpression.column();
//        } else {
//            return null;
//        }
//    }
//
//    public ConditionGroupSpecStack addJoin(final JoinNode joinNode) {
//        final OrmTable targetOrmTable;
//
//        if (joinNode.dtoClass() != null) {
//            final Class<?> joinNodeDtoClass = joinNode.dtoClass();
//            final QueryNode previousNode = joinNode.previous();
//            final OrmTable sourceOrmTable = nodeOrmTableMap.get(previousNode);
//
//            if (sourceOrmTable != null) {
//                final OrmTable contextOrmTable = sourceOrmTable.getContextTableRegistry().getOrmTable(joinNodeDtoClass);
//                targetOrmTable = contextOrmTable != null ? contextOrmTable : tableRegistry.getOrmTableOrThrow(joinNodeDtoClass);
//            } else {
//                targetOrmTable = tableRegistry.getOrmTableOrThrow(joinNodeDtoClass);
//            }
//
//            nodeOrmTableMap.put(joinNode, targetOrmTable);
//        } else {
//            targetOrmTable = null;
//        }
//
//        addJoinImpl(joinNode.type(), joinNode.dtoClass(), joinNode.table(), targetOrmTable, joinNode);
//    }
//
//    private JoinSpec addJoinImpl(final Join.JoinType type,
//                                 final @Nullable Class<?> dtoClass,
//                                 final @Nullable String rightTable,
//                                 final @Nullable OrmTable targetOrmTable,
//                                 final JoinNode joinNode) {
//        final JoinSpec joinSpec = new JoinSpec(type, dtoClass, rightTable, targetOrmTable, joinNode);
//        joinSpec.conditionGroupStack().newRootInstance();
//
//        if (this.joinSpecs == null) {
//            this.joinSpecs = new ArrayList<>();
//        }
//
//        this.joinSpecs.add(joinSpec);
//        currentJoinSpec = joinSpec;
//        return joinSpec;
//    }
//
//    public ConditionGroupSpecStack joinConditionGroupStack() {
//        return Objects.requireNonNull(currentJoinSpec, "No current JOIN")
//                .conditionGroupStack();
//    }
//
//    public void addJoinCondition(final ConditionNode conditionNode) {
//        final JoinSpec joinSpec = Objects.requireNonNull(currentJoinSpec, "No current JOIN");
//        final Object rhs;
//
//        if (conditionNode.rhsColumn() != null) {
//            final ColumnMetaData relationshipColumn = ormTable.columnMetaDataForField(conditionNode.rhsColumn());
//            final FieldAccessor relationshipFieldAccessor = ormTable.getFieldForColumnName(relationshipColumn.name());
//            rhs = new SelectFieldSpec(relationshipFieldAccessor, relationshipColumn.toColumn());
//        } else {
//            rhs = conditionNode.rhs();
//        }
//
//        joinSpec
//                .conditionGroupStack().current()
//                .newCondition(conditionNode.logicOperator(),
//                        conditionNode.lhsColumn(),
//                        conditionNode.lhsExpression(),
//                        conditionNode.operator(),
//                        rhs);
//    }
//
//    public void addWhereCondition(final ConditionNode conditionNode) {
//        ensureWhereConditionGroupStack().current()
//                .newCondition(conditionNode.logicOperator(),
//                        conditionNode.lhsColumn(),
//                        conditionNode.lhsExpression(),
//                        conditionNode.operator(),
//                        conditionNode.rhs());
//    }
//
//    public ConditionNode toConditionNode(final ConditionWithIdNode conditionWithIdNode) {
//        final String[] primaryKeyFieldNames = tableMetaData.primaryKey().stream()
//                .map(columnMetaData -> ormTable.getFieldForColumnName(columnMetaData.name()).name())
//                .toArray(String[]::new);
//
//        final Object id = conditionWithIdNode.id();
//        ConditionNode conditionNode = null;
//
//        if (primaryKeyFieldNames.length == 0) {
//            throw new IllegalArgumentException("No primary key fields found for table " + tableMetaData.name());
//        } else if (primaryKeyFieldNames.length == 1) {
//            conditionNode = new ConditionNode(null, conditionWithIdNode.logicOperator(), primaryKeyFieldNames[0], null, conditionWithIdNode.operator(), id);
//        } else {
//            // Composite PK
//            switch (id) {
//                case List<?> idList -> {
//                    if (idList.size() != primaryKeyFieldNames.length) {
//                        throw new IllegalArgumentException("Invalid number of primary key values for table %s; expected: %d, actual: %d".formatted(ormTable.getMetaData().name(), primaryKeyFieldNames.length, idList.size()));
//                    }
//
//                    for (int i = 0; i < primaryKeyFieldNames.length; i++) {
//                        final LogicOperator logicOperator = conditionNode == null ? LogicOperator.NOOP : LogicOperator.AND;
//                        conditionNode = new ConditionNode(conditionNode, logicOperator, primaryKeyFieldNames[i], null, conditionWithIdNode.operator(), idList.get(i));
//                    }
//                }
//                case Object[] idArray -> {
//                    if (idArray.length != primaryKeyFieldNames.length) {
//                        throw new IllegalArgumentException("Invalid number of primary key values for table %s; expected: %d, actual: %d".formatted(ormTable.getMetaData().name(), primaryKeyFieldNames.length, idArray.length));
//                    }
//
//                    for (int i = 1; i < primaryKeyFieldNames.length; i++) {
//                        conditionNode = new ConditionNode(conditionNode, conditionWithIdNode.logicOperator(), primaryKeyFieldNames[i], null, conditionWithIdNode.operator(), idArray[i]);
//                    }
//                }
//                case Map<?, ?> idMap -> {
//                    if (idMap.size() != primaryKeyFieldNames.length) {
//                        throw new IllegalArgumentException("Invalid number of primary key values for table %s; expected: %d, actual: %d".formatted(ormTable.getMetaData().name(), primaryKeyFieldNames.length, idMap.size()));
//                    }
//
//                    for (int i = 0; i < primaryKeyFieldNames.length; i++) {
//                        conditionNode = new ConditionNode(conditionNode, conditionWithIdNode.logicOperator(), primaryKeyFieldNames[0], null, conditionWithIdNode.operator(), idMap.get(primaryKeyFieldNames[i]));
//                    }
//                }
//                case null, default ->
//                        throw new IllegalArgumentException("Invalid composite primary key value type provided; expected: List<?>, Object[], or Map<String, ?>");
//            }
//        }
//
//        return Objects.requireNonNull(conditionNode, "Condition node not resolved for 'withId' condition");
//    }
//
//    public void addJoinCondition(final ConditionJoinUsingNode conditionJoinUsingNode) {
//        final String fieldName = Objects.requireNonNull(conditionJoinUsingNode.usingColumn(), "Using column not provided");
//
//        JoinSpec joinSpec = Objects.requireNonNull(currentJoinSpec, "No current JOIN");
//        final QueryNode sourceNode = findSourceNodeForField(joinSpec.joinNode(), fieldName);
//        final OrmTable activeSourceOrmTable = nodeOrmTableMap.get(sourceNode);
//        final Table activeSourceAliasedTable = nodeAliasedTableMap.get(sourceNode);
//
//        // Get details on the USING column on the local table
//        final MappedFieldTarget mappedFieldTarget = activeSourceOrmTable.mappedFieldTargetForField(fieldName);
//        joinSpec = Objects.requireNonNull(currentJoinSpec, "No current JOIN");
//        final Class<?> joinDtoClass = Objects.requireNonNull(joinSpec.dtoClass(), "No DTO class specified for join");
//
//        switch (mappedFieldTarget) {
//            case ColumnMetaData usingColumnMetaData -> {
//                final JoinOnSpec joinOnSpec = processOneToManyJoin(joinDtoClass, usingColumnMetaData, activeSourceAliasedTable);
//                final ConditionNode conditionNode = new ConditionNode(null, conditionJoinUsingNode.logicOperator(), null, joinOnSpec.leftSelectColumnSpec(), Operator.EQ, joinOnSpec.rightSelectColumnSpec());
//                joinSpec.setTable(joinOnSpec.rightSelectColumnSpec().getColumn().table());
//                nodeAliasedTableMap.put(joinSpec.joinNode(), joinSpec.getTable());
//                addJoinCondition(conditionNode);
//            }
//            case MappedOneToMany mappedOneToMany -> {
//                final JoinOnSpec joinOnSpec = processOneToManyReverseJoin(joinDtoClass, mappedOneToMany, activeSourceAliasedTable);
//                final ConditionNode conditionNode = new ConditionNode(null, conditionJoinUsingNode.logicOperator(), null, joinOnSpec.leftSelectColumnSpec(), Operator.EQ, joinOnSpec.rightSelectColumnSpec());
//                joinSpec.setTable(joinOnSpec.rightSelectColumnSpec().getColumn().table());
//                nodeAliasedTableMap.put(joinSpec.joinNode(), joinSpec.getTable());
//                addJoinCondition(conditionNode);
//            }
//            case MappedManyToMany mappedManyToMany -> {
//                final List<JoinOnSpec> joinOnSpecs = processManyToManyJoin(mappedManyToMany, activeSourceAliasedTable);
//
//                // First join
//                final JoinOnSpec firstJoinOnSpec = joinOnSpecs.getFirst();
//                final ConditionNode firstConditionNode = new ConditionNode(null, conditionJoinUsingNode.logicOperator(), null, firstJoinOnSpec.leftSelectColumnSpec(), Operator.EQ, firstJoinOnSpec.rightSelectColumnSpec());
//                joinSpec.setTable(firstJoinOnSpec.rightSelectColumnSpec().getColumn().table());
//                // Note: the nodeAliasedTableMap update here is for the join table, which might not be what we want for nested joins
//                addJoinCondition(firstConditionNode);
//
//                // Second join
//                joinSpec = addJoinImpl("INNER", mappedManyToMany.targetOrmTable().get().dtoClass(), null, mappedManyToMany.targetOrmTable().get(), joinSpec.joinNode());
//                final JoinOnSpec secondJoinOnSpec = joinOnSpecs.getLast();
//                final ConditionNode secondConditionNode = new ConditionNode(null, conditionJoinUsingNode.logicOperator(), null, secondJoinOnSpec.leftSelectColumnSpec(), Operator.EQ, secondJoinOnSpec.rightSelectColumnSpec());
//                joinSpec.setTable(secondJoinOnSpec.rightSelectColumnSpec().getColumn().table());
//                nodeAliasedTableMap.put(joinSpec.joinNode(), joinSpec.getTable());
//                addJoinCondition(secondConditionNode);
//            }
//            default -> throw new UnsupportedOperationException("Unsupported mapped field target: " + mappedFieldTarget);
//        }
//    }
//
//    public ConditionGroupSpecStack ensureWhereConditionGroupStack() {
//        if (where == null) {
//            where = new ConditionGroupSpecStack();
//        }
//
//        return where;
//    }
//
//    public void addGroupBy(final GroupByNode groupByNode) {
//        final List<SelectExpression> groupByExpressions;
//
//        if (groupByNode.expressions() != null) {
//            // Explicit expression
//            groupByExpressions = Arrays.stream(groupByNode.expressions())
//                    .flatMap(expressionSpec -> selectExpressionMapper
//                            .resolveProtoExpression(expressionSpec, ormTable, table, ClauseType.GROUP_BY)
//                            .stream())
//                    .map(this::resolveAlias)
//                    .map(expressionSpec -> selectExpressionMapper.toSelectExpression(expressionSpec, true))
//                    .toList();
//        } else {
//            // Column/field names
//            final String[] columnNames = Objects.requireNonNull(groupByNode.columns());
//            final SqlFunctionRegistry sqlFunctionRegistry = litebridgeContext.sqlFunctionRegistry();
//
//            if (ormTable != null) {
//                // DTO field names; translate to columns
//                groupByExpressions = Arrays.stream(columnNames)
//                        .map(fieldName -> {
//                            final Column column = ormTable.columnMetaDataForField(fieldName).toColumn();
//                            final String columnAlias = resolveAlias(table, column);
//                            return (SelectExpression) sqlFunctionRegistry.select().reference().create(column, columnAlias, fromAlias);
//                        })
//                        .toList();
//            } else {
//                // Column names
//                groupByExpressions = Arrays.stream(columnNames)
//                        .map(columnName -> {
//                            final Column column = tableMetaData.column(columnName).toColumn();
//                            final String columnAlias = resolveAlias(table, column);
//                            return (SelectExpression) sqlFunctionRegistry.select().reference().create(column, columnAlias, fromAlias);
//                        })
//                        .toList();
//            }
//        }
//
//        if (groupBy == null) {
//            groupBy = new ArrayList<>();
//        }
//
//        groupBy.addAll(groupByExpressions);
//    }
//
//    public void addHavingCondition(final ConditionNode conditionNode) {
//        ensureHavingConditionGroupStack().current()
//                .newCondition(conditionNode.logicOperator(),
//                        conditionNode.lhsColumn(),
//                        conditionNode.lhsExpression(),
//                        conditionNode.operator(),
//                        conditionNode.rhs());
//    }
//
//    public ConditionGroupSpecStack ensureHavingConditionGroupStack() {
//        if (having == null) {
//            having = new ConditionGroupSpecStack();
//        }
//
//        return having;
//    }
//
//    public void addOrderBy(final OrderByNode orderByNode) {
//        final List<SelectExpression> orderByExpressions;
//
//        if (orderByNode.expression() != null) {
//            // Explicit expression
//            orderByExpressions = selectExpressionMapper.resolveProtoExpression(orderByNode.expression(), ormTable, table, ClauseType.ORDER_BY).stream()
//                    .map(this::resolveAlias)
//                    .map(expressionSpec -> selectExpressionMapper.toSelectExpression(expressionSpec, true))
//                    .toList();
//        } else {
//            // Column/field names
//            final String columnName = Objects.requireNonNull(orderByNode.column());
//            final Column column;
//
//            if (ormTable != null) {
//                // DTO field name; translate it to a column
//                column = ormTable.columnMetaDataForField(columnName).toColumn();
//            } else {
//                // Column name
//                column = tableMetaData.column(columnName).toColumn();
//            }
//
//            final String tableAlias = aliasGenerator.tableAlias(column.table());
//            final String columnAlias = aliasGenerator.columnAlias(column);
//            orderByExpressions = Collections.singletonList(litebridgeContext.sqlFunctionRegistry().select().reference().create(column, columnAlias, tableAlias));
//        }
//
//        if (orderBys == null) {
//            orderBys = new ArrayList<>();
//        }
//
//        for (final SelectExpression orderByExpression : orderByExpressions) {
//            final OrderBy orderBy = new OrderBy(orderByExpression, orderByNode.ascending());
//            orderBys.add(orderBy);
//        }
//    }
//
//    public void setLimit(final LimitNode limitNode) {
//        this.limit = new Limit(limitNode.limit(), limitNode.offset());
//    }
//
//    @Override
//    public Select toOperation() {
//        final SelectTarget from;
//
//        if (fromQueryNode != null) {
//            final PreparedOperation preparedOperation = litebridgeContext.createQueryCompiler().compile(fromQueryNode);
//            bindValues.addAll(0, preparedOperation.bindValues());
//
//            if (fromAlias != null) {
//                from = new AliasedQuery(fromAlias, (Select) preparedOperation.operation());
//            } else {
//                from = (Select) preparedOperation.operation();
//            }
//        } else if (fromAlias != null) {
//            from = new AliasedTable(fromAlias, table);
//        } else {
//            from = table;
//        }
//
//        final ConditionGroup whereConditionGroup = where != null ? toConditionGroup(where.current(), ormTable, table) : null;
//        final ConditionGroup havingConditionGroup = having != null ? toConditionGroup(having.current(), ormTable, table) : null;
//
//        return new Select(from,
//                selectExpressions,
//                joins,
//                whereConditionGroup,
//                groupBy,
//                havingConditionGroup,
//                orderBys,
//                limit);
//    }
//
//    @Override
//    protected String resolveAlias(final Table table, final ColumnMetaData columnMetaData) {
//        return resolveAlias(table, columnMetaData.name(), columnMetaData::toColumn);
//    }
//
//    @Override
//    protected @Nullable String resolveAlias(final Table table, final Column column) {
//        return aliasGenerator.columnAlias(column);
//    }
//
//    @Override
//    protected ExpressionSpec resolveAlias(final ExpressionSpec expressionSpec) {
//        if (expressionSpec instanceof ColumnExpressionSpec columnExpressionSpec
//            && (columnExpressionSpec.getAlias() != null || columnExpressionSpec.getTableAlias() != null)) {
//            // Already aliased
//            return columnExpressionSpec;
//        }
//
//        final ColumnExpressionSpec columnExpressionSpec = findColumnExpressionSpec(expressionSpec);
//
//        if (columnExpressionSpec != null) {
//            final Column column = columnExpressionSpec.getColumn();
//            final String tableAlias = aliasGenerator.tableAlias(column.table());
//            final String columnAlias = aliasGenerator.columnAlias(column);
//            columnExpressionSpec.setAlias(columnAlias);
//            columnExpressionSpec.setTableAlias(tableAlias);
//        }
//
//        return expressionSpec;
//    }
//
//    private Table aliasTable(final Table table) {
//        tableAliases.computeIfAbsent(table, tableName -> aliasGenerator.newAlias(table.name()));
//        return table;
//    }
//
//    private Table aliasTable(final OrmTable ormTable) {
//        return aliasTable(ormTable.getMetaData().table());
//    }
//
//    private @Nullable String resolveAlias(final Table table, final String columnName) {
//        return resolveAlias(table, columnName, () -> new Column(table, columnName));
//    }
//
//    private @Nullable String resolveAlias(final Table table, final String columnName, final Supplier<Column> columnSupplier) {
//        return selectExpressions.stream()
//                .map(SelectCompilationContext::findColumn)
//                .filter(Objects::nonNull)
//                .filter(column -> table.equals(column.table()) && columnName.equals(column.name()))
//                .map(aliasGenerator::columnAlias)
//                .filter(Objects::nonNull)
//                .findFirst()
//                .orElse(null);
//    }
//
//    private ExpressionSpec aliasExpression(final ExpressionSpec expressionSpec) {
//        final ColumnExpressionSpec columnExpressionSpec = findColumnExpressionSpec(expressionSpec);
//
//        if (columnExpressionSpec != null) {
//            final Column column = columnExpressionSpec.getColumn();
//            final String tableAlias = aliasGenerator.tableAlias(column.table());
//            columnExpressionSpec.setTableAlias(tableAlias);
//
//            if (columnExpressionSpec.getAlias() != null) {
//                aliasGenerator.setColumnAlias(column, columnExpressionSpec.getAlias());
//            } else {
//                final String columnAlias = aliasGenerator.newColumnAlias(column);
//                columnExpressionSpec.setAlias(columnAlias);
//            }
//        }
//
//        return expressionSpec;
//    }
//
//    private QueryNode findSourceNodeForField(final JoinNode joinNode, final String fieldName) {
//        QueryNode current = joinNode.previous();
//
//        while (current != null) {
//            final OrmTable table = nodeOrmTableMap.get(current);
//
//            if (table != null && table.mappedFieldTargetForFieldOrNull(fieldName) != null) {
//                return current;
//            }
//
//            current = current.previous();
//        }
//
//        throw new IllegalStateException("No root node found for field name: " + fieldName);
//    }
//
//    private JoinOnSpec processOneToManyJoin(final Class<?> joinDtoClass, final ColumnMetaData leftColumnMetaData, final Table leftAliasedTable) {
//        // Left column
//        final Column leftColumn = leftColumnMetaData.toColumn();
//        final String leftTableAlias = aliasGenerator.tableAlias(leftAliasedTable);
//        final String leftColumnAlias = resolveAlias(leftAliasedTable, leftColumn);
//        final SelectColumnSpec leftSelectColumnSpec = new SelectColumnSpec(leftColumn, leftColumnAlias, leftTableAlias);
//
//        // Right table & column
//        final JoinSpec joinSpec = Objects.requireNonNull(currentJoinSpec, "No current JOIN");
//        OrmTable rightOrmTable = joinSpec.ormTable();
//
//        if (rightOrmTable == null) {
//            rightOrmTable = tableRegistry.getOrmTable(Objects.requireNonNull(joinDtoClass));
//        }
//
//        final Table aliasedRightTable = aliasTable(rightOrmTable);
//        final TableMetaData rightTableMetaData = rightOrmTable.getMetaData();
//        final ColumnMetaData rightColumnMetaData = rightTableMetaData.column(leftColumnMetaData.getJoinColumn());
//
//        // Add right table columns to select
//        SelectColumnSpec rightSelectColumnSpec = null;
//
//        if (selectAll) {
//            final SqlFunctionRegistry sqlFunctionRegistry = litebridgeContext.sqlFunctionRegistry();
//
//            for (ColumnMetaData columnMetaData : rightTableMetaData.columns()) {
//                final Column column = columnMetaData.toColumn();
//                final String columnAlias = aliasGenerator.newColumnAlias(column);
//                final String tableAlias = aliasGenerator.newTableAlias(column.table());
//                this.selectExpressions.add(sqlFunctionRegistry.select().column().create(column, columnAlias, tableAlias));
//
//                if (columnMetaData.equals(rightColumnMetaData)) {
//                    // Don't include the column alias for the JOIN clause
//                    rightSelectColumnSpec = new SelectColumnSpec(column, null, tableAlias);
//                }
//            }
//        }
//
//        return new JoinOnSpec(leftSelectColumnSpec, Objects.requireNonNull(rightSelectColumnSpec, "Right JOIN column not selected"));
//    }
//
//    private List<JoinOnSpec> processManyToManyJoin(final MappedManyToMany mappedManyToMany, final Table leftAliasedTable) {
//        final JoinOnSpec leftLeftJoinOnSpec = createManyToManyLeftJoinOnSpec(mappedManyToMany, leftAliasedTable);
//        return List.of(leftLeftJoinOnSpec, createManyToManyRightJoinOnSpec(mappedManyToMany, leftLeftJoinOnSpec.rightSelectColumnSpec().getColumn().table()));
//    }
//
//    private @NonNull JoinOnSpec createManyToManyLeftJoinOnSpec(final MappedManyToMany mappedManyToMany, final Table leftAliasedTable) {
//        // Left column
//        final JoinSpec joinSpec = Objects.requireNonNull(currentJoinSpec, "No current JOIN");
//        final OrmTable leftOrmTable = nodeOrmTableMap.get(findSourceNodeForField(joinSpec.joinNode(), joinSpec.joinNode().condition() != null ? ((ConditionJoinUsingNode) joinSpec.joinNode().condition()).usingColumn() : ""));
//        final TableMetaData leftTableMetaData = (leftOrmTable != null ? leftOrmTable : ormTable).getMetaData();
//
//        if (leftTableMetaData.primaryKey().isEmpty()) {
//            throw new IllegalArgumentException("Left table " + leftTableMetaData.name() + " does not have a primary key; cannot map many-to-many join: " + mappedManyToMany);
//        }
//
//        //TODO: add support for composite primary keys in many-to-many joins
//        final ColumnMetaData leftColumnMetaData = leftTableMetaData.primaryKey().getFirst();
////        final SelectColumnSpec leftSelectColumnSpec = new SelectColumnSpec(resolveAlias(leftAliasedTable, leftColumnMetaData));
////
////        // Join table & column - alias it directly in order to support self-references
////        final Table aliasedJoinTable = aliasTable(mappedManyToMany.joinOrmTable());
////        final Column aliasedJoinColumn = resolveAlias(aliasedJoinTable, mappedManyToMany.joinColumn());
////        final SelectColumnSpec joinSelectColumnSpec = new SelectColumnSpec(aliasedJoinColumn);
////
////        return new JoinOnSpec(leftSelectColumnSpec, joinSelectColumnSpec);
//        throw new UnsupportedOperationException("Not implemented yet");
//    }
//
//    private @NonNull JoinOnSpec createManyToManyRightJoinOnSpec(final MappedManyToMany mappedManyToMany, final Table aliasedJoinTable) {
//        // Join table & column
////        final SelectColumnSpec joinSelectColumnSpec = new SelectColumnSpec(resolveAlias(aliasedJoinTable, mappedManyToMany.inverseJoinColumn()));
////
////        // Right column
////        final OrmTable rightOrmTable = mappedManyToMany.targetOrmTable().get();
////        final TableMetaData rightTableMetaData = rightOrmTable.getMetaData();
////
////        if (rightTableMetaData.primaryKey().isEmpty()) {
////            throw new IllegalArgumentException("Right table " + tableMetaData.name() + " does not have a primary key; cannot map many-to-many join: " + mappedManyToMany);
////        }
////
////        //TODO: add support for composite primary keys in many-to-many joins
////        final ColumnMetaData rightColumnMetaData = rightTableMetaData.primaryKey().getFirst();
////        final Table aliasedRightTable = aliasTable(rightOrmTable);
////
////        // Add joined table columns to select
////        SelectColumnSpec rightSelectColumnSpec = null;
////
////        if (selectAll) {
////            final SqlFunctionRegistry sqlFunctionRegistry = litebridgeContext.sqlFunctionRegistry();
////
////            for (ColumnMetaData columnMetaData : rightOrmTable.mappedColumns()) {
////                final Column column = columnMetaData.toColumn();
////                final String columnAlias = aliasGenerator.newColumnAlias(column);
////                final String tableAlias = aliasGenerator.newTableAlias(column.table());
////                this.selectExpressions.add(sqlFunctionRegistry.select().column().create(column, columnAlias, tableAlias));
////
////                if (columnMetaData.equals(rightColumnMetaData)) {
////                    rightSelectColumnSpec = new SelectColumnSpec(column, columnAlias);
////                }
////            }
////        }
////
////        return new JoinOnSpec(joinSelectColumnSpec, rightSelectColumnSpec);
//        throw new UnsupportedOperationException("Not implemented yet");
//    }
//
//    private JoinOnSpec processOneToManyReverseJoin(final Class<?> joinDtoClass, final MappedOneToMany mappedOneToMany, final Table leftTable) {
//        // Join table & column
//        final JoinSpec joinSpec = Objects.requireNonNull(currentJoinSpec, "No current JOIN");
//        OrmTable rightOrmTable = joinSpec.ormTable();
//
//        if (rightOrmTable == null) {
//            rightOrmTable = litebridgeContext.tableRegistry().getOrmTableOrThrow(Objects.requireNonNull(joinDtoClass));
//        }
//
//        final Table aliasedRightTable = aliasTable(rightOrmTable);
//        final ColumnMetaData rightColumnMetaData = rightOrmTable.columnMetaDataForField(mappedOneToMany.mappedByField());
//
//        // Local column
//        //TODO: composite primary keys
//        final OrmTable leftOrmTable = nodeOrmTableMap.get(findSourceNodeForField(joinSpec.joinNode(), joinSpec.joinNode().condition() != null ? ((ConditionJoinUsingNode) joinSpec.joinNode().condition()).usingColumn() : "")); // Rough but okay for reverse
//        final ColumnMetaData leftColumnMetaData = (leftOrmTable != null ? leftOrmTable : ormTable).getMetaData().primaryKey().getFirst();
//        final Column leftColumn = leftColumnMetaData.toColumn();
//        final String leftTableAlias = aliasGenerator.tableAlias(leftTable);
//        final String leftColumnAlias = resolveAlias(leftTable, leftColumnMetaData);
//        final SelectColumnSpec leftSelectColumnSpec = new SelectColumnSpec(leftColumn, leftColumnAlias, leftTableAlias);
//
//        // Add join table columns to select
//        SelectColumnSpec rightSelectColumnSpec = null;
//
//        if (selectAll) {
//            final SqlFunctionRegistry sqlFunctionRegistry = litebridgeContext.sqlFunctionRegistry();
//
//            for (ColumnMetaData columnMetaData : rightOrmTable.getMetaData().columns()) {
//                final Column column = columnMetaData.toColumn();
//                final String columnAlias = aliasGenerator.newColumnAlias(column);
//                final String tableAlias = aliasGenerator.newTableAlias(column.table());
//                this.selectExpressions.add(sqlFunctionRegistry.select().column().create(column, columnAlias, tableAlias));
//
//                if (columnMetaData.equals(rightColumnMetaData)) {
//                    // Don't include the column alias for the JOIN clause
//                    rightSelectColumnSpec = new SelectColumnSpec(column, null, tableAlias);
//                }
//            }
//        }
//
//        return new JoinOnSpec(leftSelectColumnSpec, Objects.requireNonNull(rightSelectColumnSpec, "Right JOIN column not selected"));
//    }
//
//    private record JoinOnSpec(SelectColumnSpec leftSelectColumnSpec, SelectColumnSpec rightSelectColumnSpec) {
//    }
//}
