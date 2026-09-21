package org.litebridge.orm.engine.compiler;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.MappedFieldTarget;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.VirtualTable;
import org.litebridge.db.spi.alias.AliasedTable;
import org.litebridge.db.spi.expression.BindValueExpression;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.expression.DelegateExpression;
import org.litebridge.db.spi.expression.LiteralExpression;
import org.litebridge.db.spi.expression.SelectExpression;
import org.litebridge.db.spi.expression.SqlFunctionRegistry;
import org.litebridge.db.spi.query.ConditionGroup;
import org.litebridge.db.spi.query.Join;
import org.litebridge.db.spi.query.Limit;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.db.spi.query.OrderBy;
import org.litebridge.db.spi.query.Select;
import org.litebridge.db.spi.query.SelectTarget;
import org.litebridge.orm.api.select.model.ConditionGroupSpec;
import org.litebridge.orm.api.select.model.SelectExpressionMapper;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.ConditionJoinUsingNode;
import org.litebridge.orm.engine.ast.ConditionWithIdNode;
import org.litebridge.orm.engine.ast.GroupByNode;
import org.litebridge.orm.engine.ast.HavingNode;
import org.litebridge.orm.engine.ast.JoinNode;
import org.litebridge.orm.engine.ast.LimitNode;
import org.litebridge.orm.engine.ast.OrderByNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.ast.SelectNode;
import org.litebridge.orm.engine.ast.WhereNode;
import org.litebridge.orm.expression.ColumnExpressionSpec;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.intent.ConvertSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;
import org.litebridge.orm.persistence.MappedManyToMany;
import org.litebridge.orm.persistence.MappedOneToMany;
import org.litebridge.orm.persistence.OrmTable;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Compilation context for SELECT statements.
 */
final class SelectCompilationContext extends AbstractCompilationContext {

    private final SelectNode selectNode;
    private @Nullable List<JoinSpec> joinSpecs;
    private @Nullable ConditionWithIdNode whereConditionWithIdNode;
    private @Nullable ConditionGroupSpecStack whereConditionGroupSpecStack;
    private @Nullable List<GroupByNode> groupByNodes;
    private @Nullable ConditionGroupSpecStack havingConditionGroupSpecStack;
    private @Nullable List<OrderByNode> orderByNodes;
    private @Nullable Limit limit;
    private @Nullable List<SelectExpression> joinSelectExpressions;

    SelectCompilationContext(final SelectNode selectNode,
                             final LitebridgeContext litebridgeContext) {
        super(litebridgeContext);
        this.selectNode = selectNode;
    }

    public ConditionGroupSpecStack addJoin(final JoinNode joinNode) {
        if (joinSpecs == null) {
            joinSpecs = new ArrayList<>();
        }

        final JoinSpec joinSpec = new JoinSpec(joinNode);
        joinSpecs.add(joinSpec);
        return joinSpec.conditionGroupStack();
    }

    public void addJoinUsingCondition(final ConditionJoinUsingNode conditionJoinUsingNode) {
        final JoinSpec joinSpec = Objects.requireNonNull(joinSpecs).getLast();
        joinSpec.setConditionJoinUsingNode(conditionJoinUsingNode);
    }

    public ConditionGroupSpecStack setWhereNode(final WhereNode whereNode) {
        whereConditionGroupSpecStack = new ConditionGroupSpecStack();
        return whereConditionGroupSpecStack;
    }

    public void setWhereConditionWithIdNode(final ConditionWithIdNode conditionWithIdNode) {
        this.whereConditionWithIdNode = conditionWithIdNode;
    }

    public void addGroupByNode(final GroupByNode groupByNode) {
        if (groupByNodes == null) {
            groupByNodes = new ArrayList<>();
        }

        groupByNodes.add(groupByNode);
    }

    public ConditionGroupSpecStack setHavingNode(final HavingNode havingNode) {
        havingConditionGroupSpecStack = new ConditionGroupSpecStack();
        return havingConditionGroupSpecStack;
    }

    public void addOrderByNode(final OrderByNode orderByNode) {
        if (orderByNodes == null) {
            orderByNodes = new ArrayList<>();
        }

        orderByNodes.add(orderByNode);
    }

    public void setLimitNode(final LimitNode limitNode) {
        this.limit = new Limit(limitNode.limit(), limitNode.offset());
    }

    @Override
    public Operation toOperation() {
        // Process nodes in SQL execution order
        final SelectTarget from = processFromClause();
        final List<Join> joins = processJoinClauses(from);

        final List<SelectTarget> selectTargets;

        if (joins != null) {
            selectTargets = new ArrayList<>();
            selectTargets.add(from);

            for (Join join : joins) {
                selectTargets.add(join.target());
            }
        } else {
            selectTargets = Collections.singletonList(from);
        }

        final ConditionGroup where = processWhereClause(selectTargets);
        final List<SelectExpression> groupBy = processGroupByClauses(from);
        final ConditionGroup having = processHavingClause(selectTargets);
        final List<OrderBy> orderBys = processOrderByClauses(from);
        final List<SelectExpression> selectExpressions = processSelectExpressions(from);

        return new Select(from, selectExpressions, joins, where, groupBy, having, orderBys, limit);
    }

    private SelectTarget processFromClause() {
        return getSelectTarget(selectNode.dtoClass(),
                selectNode.contextDtoClass(),
                selectNode.table(),
                selectNode.fromQueryNode(),
                selectNode.alias());
    }

    private @Nullable List<Join> processJoinClauses(final SelectTarget from) {
        if (joinSpecs == null) {
            return null;
        }

        final List<Join> joins = new ArrayList<>(joinSpecs.size());

        for (int i = 0; i < joinSpecs.size(); i++) {
            final JoinSpec joinSpec = joinSpecs.get(i);
            joins.add(processJoinClause(joinSpec, from));
        }

        return joins;
    }

    private Join processJoinClause(final JoinSpec joinSpec, final SelectTarget from) {
        final JoinNode joinNode = joinSpec.joinNode();

        // Determine the JOIN target
        SelectTarget joinTarget = getSelectTarget(joinNode.dtoClass(),
                joinNode.contextDtoClass(),
                joinNode.table(),
                joinNode.queryNode(),
                joinNode.alias());

        // Compile condition specs and create new Join
        if (joinSpec.getConditionJoinUsingNode() != null) {
            joinTarget = processJoinUsingConditionNode(joinSpec.getConditionJoinUsingNode(), joinTarget, joinSpec, from);
        }

        final ConditionGroup conditionGroup = toConditionGroup(joinSpec.conditionGroupStack().current(), List.of(from, joinTarget));
        return new Join(joinNode.type(), joinTarget, conditionGroup);
    }

    private SelectTarget processJoinUsingConditionNode(final ConditionJoinUsingNode conditionJoinUsingNode,
                                                       final SelectTarget joinTarget,
                                                       final JoinSpec joinSpec,
                                                       final SelectTarget from) {
        final String fieldName = Objects.requireNonNull(conditionJoinUsingNode.usingColumn(), "USING column not provided");
        final Table leftTable = getTable(from);
        final OrmTable leftOrmTable = litebridgeContext.tableRegistry().getOrmTableOrThrow(leftTable);

        // Get details on the USING column on the local table
        final MappedFieldTarget mappedFieldTarget = leftOrmTable.mappedFieldTargetForField(fieldName);
        final ConditionGroupSpec conditionGroupSpec = joinSpec.conditionGroupStack().current();

        switch (mappedFieldTarget) {
            case ColumnMetaData usingColumnMetaData -> {
                final JoinOnSpec joinOnSpec = processOneToManyJoin(usingColumnMetaData, joinTarget);
                final SelectColumnSpec leftSelectColumnSpec = joinOnSpec.leftSelectColumnSpec();
                final SelectColumnSpec rightSelectColumnSpec = joinOnSpec.rightSelectColumnSpec();

                if (conditionGroupSpec.isEmpty()
                        && leftSelectColumnSpec.getColumn().name().equals(rightSelectColumnSpec.getColumn().name())) {
                    // No other conditions, and column names match; use USING
                    final SelectColumnSpec usingColumnSelectSpec = new SelectColumnSpec(
                            new Column(VirtualTable.anonymous(), leftSelectColumnSpec.getColumn().name()));

                    conditionGroupSpec.newCondition(conditionJoinUsingNode.logicOperator(),
                            null,
                            usingColumnSelectSpec,
                            Operator.USING,
                            usingColumnSelectSpec);
                } else {
                    conditionGroupSpec.newCondition(conditionJoinUsingNode.logicOperator(),
                            null,
                            leftSelectColumnSpec,
                            Operator.EQ,
                            rightSelectColumnSpec);
                }
            }
            case MappedOneToMany mappedOneToMany -> {
                final JoinOnSpec joinOnSpec = processOneToManyReverseJoin(joinTarget, mappedOneToMany, from, true);
                final SelectColumnSpec leftSelectColumnSpec = joinOnSpec.leftSelectColumnSpec();
                final SelectColumnSpec rightSelectColumnSpec = joinOnSpec.rightSelectColumnSpec();

                if (conditionGroupSpec.isEmpty()
                        && leftSelectColumnSpec.getColumn().name().equals(rightSelectColumnSpec.getColumn().name())) {
                    // No other conditions, and column names match; use USING
                    final SelectColumnSpec usingColumnSelectSpec = new SelectColumnSpec(
                            new Column(VirtualTable.anonymous(), leftSelectColumnSpec.getColumn().name()));

                    conditionGroupSpec.newCondition(conditionJoinUsingNode.logicOperator(),
                            null,
                            usingColumnSelectSpec,
                            Operator.USING,
                            usingColumnSelectSpec);
                } else {
                    conditionGroupSpec.newCondition(conditionJoinUsingNode.logicOperator(),
                            null,
                            leftSelectColumnSpec,
                            Operator.EQ,
                            rightSelectColumnSpec);
                }
            }
            case MappedManyToMany mappedManyToMany -> {
                final List<JoinOnSpec> joinOnSpecs = processManyToManyJoin(mappedManyToMany, from);
                final Table joinTable;

                // First join
                {
                    final JoinOnSpec firstJoinOnSpec = joinOnSpecs.getFirst();
                    final SelectColumnSpec leftSelectColumnSpec = firstJoinOnSpec.leftSelectColumnSpec();
                    final SelectColumnSpec rightSelectColumnSpec = firstJoinOnSpec.rightSelectColumnSpec();
                    joinTable = rightSelectColumnSpec.getColumn().table();

                    if (conditionGroupSpec.isEmpty()
                            && leftSelectColumnSpec.getColumn().name().equals(rightSelectColumnSpec.getColumn().name())) {
                        // No other conditions, and column names match; use USING
                        final SelectColumnSpec usingColumnSelectSpec = new SelectColumnSpec(
                                new Column(VirtualTable.anonymous(), leftSelectColumnSpec.getColumn().name()));

                        conditionGroupSpec.newCondition(conditionJoinUsingNode.logicOperator(),
                                null,
                                usingColumnSelectSpec,
                                Operator.USING,
                                usingColumnSelectSpec);
                    } else {
                        conditionGroupSpec.newCondition(conditionJoinUsingNode.logicOperator(),
                                null,
                                leftSelectColumnSpec,
                                Operator.EQ,
                                rightSelectColumnSpec);
                    }
                }

                // Second join
                final JoinOnSpec secondJoinOnSpec = joinOnSpecs.getLast();
                final Class<?> rightDtoClass = mappedManyToMany.targetOrmTable().get().dtoClass();
                final JoinNode secondJoinNode = new JoinNode(null, joinSpec.type(), rightDtoClass, null, null, null, null);
                final JoinSpec secondJoinSpec = new JoinSpec(secondJoinNode);
                final ConditionGroupSpec secondConditionGroupSpec = secondJoinSpec.conditionGroupStack().current();

                final SelectColumnSpec leftSelectColumnSpec = secondJoinOnSpec.leftSelectColumnSpec();
                final SelectColumnSpec rightSelectColumnSpec = secondJoinOnSpec.rightSelectColumnSpec();
                secondConditionGroupSpec.newCondition(conditionJoinUsingNode.logicOperator(),
                        null,
                        leftSelectColumnSpec,
                        Operator.EQ,
                        rightSelectColumnSpec);

                joinSpecs.add(secondJoinSpec);

                final String joinTableAlias = aliasGenerator.tableAlias(joinTable);
                return joinTableAlias != null ? new AliasedTable(joinTableAlias, joinTable) : joinTable;
            }
            default -> throw new UnsupportedOperationException("Unsupported mapped field target: " + mappedFieldTarget);
        }

        return joinTarget;
    }

    private @Nullable ConditionGroup processWhereClause(final List<SelectTarget> selectTargets) {
        if (whereConditionWithIdNode != null) {
            // Just use the FROM clause as there are no further WHERE conditions if withId() is used
            processWhereConditionWithIdNode(whereConditionWithIdNode, selectTargets.getFirst());
        }

        return whereConditionGroupSpecStack != null ? toConditionGroup(whereConditionGroupSpecStack.current(), selectTargets) : null;
    }

    private void processWhereConditionWithIdNode(final ConditionWithIdNode conditionWithIdNode, final SelectTarget selectTarget) {
        final Table table = getTable(selectTarget);
        final OrmTable ormTable = tableRegistry.getOrmTableOrThrow(table);
        final TableMetaData tableMetaData = ormTable.getMetaData();
        final ConditionGroupSpec conditionGroupSpec = whereConditionGroupSpecStack.current();

        final String[] primaryKeyFieldNames = tableMetaData.primaryKey().stream()
                .map(columnMetaData -> ormTable.getFieldForColumnName(columnMetaData.name()).name())
                .toArray(String[]::new);

        final Object id = conditionWithIdNode.id();

        if (primaryKeyFieldNames.length == 0) {
            throw new IllegalArgumentException("No primary key fields found for table " + tableMetaData.name());
        } else if (primaryKeyFieldNames.length == 1) {
            conditionGroupSpec.newCondition(conditionWithIdNode.logicOperator(), primaryKeyFieldNames[0], null, conditionWithIdNode.operator(), id);
        } else {
            // Composite PK
            switch (id) {
                case List<?> idList -> {
                    if (idList.size() != primaryKeyFieldNames.length) {
                        throw new IllegalArgumentException("Invalid number of primary key values for table %s; expected: %d, actual: %d".formatted(ormTable.getMetaData().name(), primaryKeyFieldNames.length, idList.size()));
                    }

                    for (int i = 0; i < primaryKeyFieldNames.length; i++) {
                        final LogicOperator logicOperator = i == 0 ? LogicOperator.NOOP : LogicOperator.AND;
                        conditionGroupSpec.newCondition(logicOperator, primaryKeyFieldNames[i], null, conditionWithIdNode.operator(), idList.get(i));
                    }
                }
                case Object[] idArray -> {
                    if (idArray.length != primaryKeyFieldNames.length) {
                        throw new IllegalArgumentException("Invalid number of primary key values for table %s; expected: %d, actual: %d".formatted(ormTable.getMetaData().name(), primaryKeyFieldNames.length, idArray.length));
                    }

                    for (int i = 0; i < primaryKeyFieldNames.length; i++) {
                        final LogicOperator logicOperator = i == 0 ? LogicOperator.NOOP : LogicOperator.AND;
                        conditionGroupSpec.newCondition(logicOperator, primaryKeyFieldNames[i], null, conditionWithIdNode.operator(), idArray[i]);
                    }
                }
                case Map<?, ?> idMap -> {
                    if (idMap.size() != primaryKeyFieldNames.length) {
                        throw new IllegalArgumentException("Invalid number of primary key values for table %s; expected: %d, actual: %d".formatted(ormTable.getMetaData().name(), primaryKeyFieldNames.length, idMap.size()));
                    }

                    for (int i = 0; i < primaryKeyFieldNames.length; i++) {
                        final LogicOperator logicOperator = i == 0 ? LogicOperator.NOOP : LogicOperator.AND;
                        conditionGroupSpec.newCondition(logicOperator, primaryKeyFieldNames[i], null, conditionWithIdNode.operator(), idMap.get(primaryKeyFieldNames[i]));
                    }
                }
                case null, default ->
                        throw new IllegalArgumentException("Invalid composite primary key value type provided; expected: List<?>, Object[], or Map<String, ?>");
            }
        }
    }

    private @Nullable List<SelectExpression> processGroupByClauses(final SelectTarget selectTarget) {
        if (groupByNodes == null) {
            return null;
        }

        return groupByNodes.stream()
                .flatMap(groupByNode -> processGroupByClause(groupByNode, selectTarget).stream())
                .toList();
    }

    private List<SelectExpression> processGroupByClause(final GroupByNode groupByNode, final SelectTarget selectTarget) {
        final Table table = getTable(selectTarget);
        final OrmTable ormTable = tableRegistry.getOrmTable(table);
        final String tableAlias = getAlias(selectTarget);
        final SelectExpressionMapper selectExpressionMapper = litebridgeContext.selectExpressionMapper();
        final List<SelectExpression> groupByExpressions;

        if (groupByNode.expressions() != null) {
            // Explicit expression
            groupByExpressions = Arrays.stream(groupByNode.expressions())
                    .flatMap(expressionSpec -> selectExpressionMapper
                            .resolveProtoExpression(expressionSpec, ormTable, table, tableAlias, ClauseType.GROUP_BY)
                            .stream())
                    .map(expressionSpec -> selectExpressionMapper.toSelectExpression(expressionSpec, true))
                    .toList();
        } else {
            // Column/field names
            final String[] columnNames = Objects.requireNonNull(groupByNode.columns());
            final SqlFunctionRegistry sqlFunctionRegistry = litebridgeContext.sqlFunctionRegistry();

            if (ormTable != null) {
                // DTO field names; translate to columns
                groupByExpressions = Arrays.stream(columnNames)
                        .map(fieldName -> {
                            final Column column = ormTable.columnMetaDataForField(fieldName).column();
                            final String columnAlias = aliasGenerator.columnAlias(column);
                            return (SelectExpression) sqlFunctionRegistry.select().reference().create(column, columnAlias, tableAlias);
                        })
                        .toList();
            } else {
                // Column names
                final TableMetaData tableMetaData = getTableMetaData(table);
                groupByExpressions = Arrays.stream(columnNames)
                        .map(columnName -> {
                            final Column column = tableMetaData.column(columnName).column();
                            final String columnAlias = aliasGenerator.columnAlias(column);
                            return (SelectExpression) sqlFunctionRegistry.select().reference().create(column, columnAlias, tableAlias);
                        })
                        .toList();
            }
        }

        return groupByExpressions;
    }

    private @Nullable ConditionGroup processHavingClause(final List<SelectTarget> selectTargets) {
        return havingConditionGroupSpecStack != null ? toConditionGroup(havingConditionGroupSpecStack.current(), selectTargets) : null;
    }

    private List<OrderBy> processOrderByClauses(final SelectTarget selectTarget) {
        if (orderByNodes == null) {
            return null;
        }

        return orderByNodes.stream()
                .flatMap(orderByNode -> processOrderByClause(orderByNode, selectTarget))
                .toList();
    }

    private Stream<OrderBy> processOrderByClause(final OrderByNode orderByNode, final SelectTarget selectTarget) {
        final Table table = getTable(selectTarget);
        final SelectExpressionMapper selectExpressionMapper = litebridgeContext.selectExpressionMapper();
        final List<SelectExpression> orderByExpressions;

        if (orderByNode.expression() != null) {
            // Explicit expression
            final OrmTable ormTable = tableRegistry.getOrmTable(table);
            final String tableAlias = getAlias(selectTarget);

            orderByExpressions = selectExpressionMapper.resolveProtoExpression(orderByNode.expression(), ormTable, table, tableAlias, ClauseType.ORDER_BY).stream()
                    .map(expressionSpec -> selectExpressionMapper.toSelectExpression(expressionSpec, true))
                    .toList();
        } else {
            // Column/field names
            final String columnName = Objects.requireNonNull(orderByNode.column());
            final Column column;

            if (litebridgeContext.mode() == LitebridgeContext.Mode.DTO) {
                // DTO field name; translate it to a column
                final OrmTable ormTable = tableRegistry.getOrmTableOrThrow(table);
                column = ormTable.columnMetaDataForField(columnName).column();
            } else {
                // Column name
                final TableMetaData tableMetaData = getTableMetaData(table);
                column = tableMetaData.column(columnName).column();
            }

            final String tableAlias = aliasGenerator.tableAlias(column.table());
            final String columnAlias = aliasGenerator.columnAlias(column);
            orderByExpressions = Collections.singletonList(litebridgeContext.sqlFunctionRegistry().select().reference().create(column, columnAlias, tableAlias));
        }

        return orderByExpressions.stream()
                .map(orderByExpression -> new OrderBy(orderByExpression, orderByNode.ascending()));
    }

    private List<SelectExpression> processSelectExpressions(final SelectTarget from) {
        final List<SelectExpression> selectExpressions;

        if (selectNode.isSelectAll()) {
            selectExpressions = createSelectExpressionsAll(from);
        } else if (selectNode.columns() != null) {
            // Specific field/column names specified
            selectExpressions = createSelectExpressionsColumns(selectNode.columns(), from);
        } else {
            // Specific expression specifications
            selectExpressions = createSelectExpressionsExpressionSpecs(Objects.requireNonNull(selectNode.expressions()), from);
        }

        if (joinSelectExpressions != null) {
            selectExpressions.addAll(joinSelectExpressions);
        }

        return selectExpressions;
    }

    private List<SelectExpression> createSelectExpressionsAll(final SelectTarget selectTarget) {
        final Table table = getTable(selectTarget);
        final List<ColumnMetaData> columnMetaDatas;

        if (litebridgeContext.mode() == LitebridgeContext.Mode.DTO) {
            // All mapped fields
            final OrmTable ormTable = tableRegistry.getOrmTableOrThrow(table);
            columnMetaDatas = ormTable.mappedColumns();
        } else {
            // All columns; just select *, don't specify columns
            columnMetaDatas = Collections.emptyList();
        }

        final SqlFunctionRegistry sqlFunctionRegistry = litebridgeContext.sqlFunctionRegistry();
        final List<SelectExpression> selectExpressions = new ArrayList<>(columnMetaDatas.size());
        final String fromAlias = getAlias(selectTarget);

        for (ColumnMetaData columnMetaData : columnMetaDatas) {
            final Column column = columnMetaData.column();
            final String columnAlias;
            final String tableAlias;

            if (fromAlias != null) {
                columnAlias = aliasGenerator.newColumnAlias(column);
                tableAlias = aliasGenerator.newTableAlias(column.table());
            } else {
                columnAlias = null;
                tableAlias = null;
            }

            selectExpressions.add(sqlFunctionRegistry.select().column().create(column, columnAlias, tableAlias));
        }

        return selectExpressions;
    }

    private List<SelectExpression> createSelectExpressionsColumns(final String[] columns, final SelectTarget selectTarget) {
        final SqlFunctionRegistry sqlFunctionRegistry = litebridgeContext.sqlFunctionRegistry();
        final Table table = getTable(selectTarget);
        final String tableAlias = getAlias(selectTarget);
        final List<SelectExpression> selectExpressions = new ArrayList<>(columns.length);

        if (litebridgeContext.mode() == LitebridgeContext.Mode.DTO) {
            final OrmTable ormTable = tableRegistry.getOrmTableOrThrow(table);

            // Translate field names to column names
            for (final String fieldName : columns) {
                final ColumnMetaData columnMetaData = ormTable.columnMetaDataForField(fieldName);
                final Column column = columnMetaData.column();
                final String columnAlias = aliasGenerator.newColumnAlias(column);
                selectExpressions.add(sqlFunctionRegistry.select().column().create(column, columnAlias, tableAlias));
            }
        } else {
            final TableMetaData tableMetaData = getTableMetaData(table);

            for (final String columnName : columns) {
                final ColumnMetaData columnMetaData = tableMetaData.column(columnName);
                final Column column = columnMetaData.column();
                selectExpressions.add(sqlFunctionRegistry.select().column().create(column, null, tableAlias));
            }
        }

        return selectExpressions;
    }

    private List<SelectExpression> createSelectExpressionsExpressionSpecs(final ExpressionSpec[] expressionSpecs, final SelectTarget selectTarget) {
        final SelectExpressionMapper selectExpressionMapper = litebridgeContext.selectExpressionMapper();
        final Table table = getTable(selectTarget);
        final OrmTable ormTable = tableRegistry.getOrmTable(table);
        final String tableAlias = getAlias(selectTarget);

        final List<ExpressionSpec> resolvedExpressionSpecs = new ArrayList<>(expressionSpecs.length);

        for (ExpressionSpec expressionSpec : expressionSpecs) {
            resolvedExpressionSpecs.addAll(selectExpressionMapper.resolveProtoExpression(expressionSpec, ormTable, table, tableAlias, ClauseType.SELECT));
        }

        Stream<ExpressionSpec> expressionSpecStream = resolvedExpressionSpecs.stream();

        if (litebridgeContext.mode() == LitebridgeContext.Mode.DTO) {
            expressionSpecStream = expressionSpecStream.map(this::aliasExpression);
        }

        final List<SelectExpression> selectExpressions = new ArrayList<>();

        expressionSpecStream.forEach(expressionSpec -> {
            final SelectExpression selectExpression = selectExpressionMapper.toSelectExpression(expressionSpec, false);

            if (selectExpression instanceof LiteralExpression literalExpression && literalExpression.isParameter()) {
                final Object value = literalExpression.value();
                final BindValueExpression bindValueExpression = createBindValueExpression(value, bindValues.size());
                bindValues.addAll(createBindValues(literalExpression, value, litebridgeContext.tableMetaDataCache(), litebridgeContext.typeConverter()));
                final int dataType = value != null ? litebridgeContext.typeConverter().getSqlDataType(value.getClass()) : Types.NULL;
                final DelegateExpression castExpression = litebridgeContext.sqlFunctionRegistry().cast().create(bindValueExpression, literalExpression.alias(), dataType);
                selectExpressions.add(castExpression);
            } else {
                selectExpressions.add(selectExpression);
            }
        });

        return selectExpressions;
    }

    private SelectTarget getSelectTarget(final @Nullable Class<?> dtoClass,
                                         final @Nullable Class<?> contextDtoClass,
                                         final @Nullable String tableName,
                                         final @Nullable QueryNode fromQueryNode,
                                         final @Nullable String alias) {
        final SelectTarget selectTarget;

        if (dtoClass != null) {
            // Selecting a DTO/entity
            selectTarget = getSelectTargetDto(dtoClass, contextDtoClass, alias);
        } else if (tableName != null) {
            // Selecting a table directly
            selectTarget = getSelectTargetTable(tableName, alias);
        } else if (fromQueryNode != null) {
            // Selecting from a subquery
            selectTarget = getSelectTargetQuery(fromQueryNode, alias);
        } else {
            // Select without a source table; database providers handle this differently
            selectTarget = SelectTarget.voidTarget();
        }

        return selectTarget;
    }

    private ExpressionSpec aliasExpression(final ExpressionSpec expressionSpec) {
        final ColumnExpressionSpec columnExpressionSpec = findColumnExpressionSpec(expressionSpec);

        if (columnExpressionSpec != null) {
            final Column column = columnExpressionSpec.getColumn();
            final String tableAlias = aliasGenerator.tableAlias(column.table());
            columnExpressionSpec.setTableAlias(tableAlias);

            if (columnExpressionSpec.getAlias() != null) {
                aliasGenerator.setColumnAlias(column, columnExpressionSpec.getAlias());
            } else {
                final String columnAlias = aliasGenerator.newColumnAlias(column);
                columnExpressionSpec.setAlias(columnAlias);
            }
        }

        return expressionSpec;
    }

    private JoinOnSpec processOneToManyJoin(final ColumnMetaData leftColumnMetaData, final SelectTarget rightSelectTarget) {
        // Left column
        final Column leftColumn = leftColumnMetaData.column();
        final String leftTableAlias = aliasGenerator.tableAlias(leftColumnMetaData.table());
        final String leftColumnAlias = aliasGenerator.columnAlias(leftColumn);
        final SelectColumnSpec leftSelectColumnSpec = new SelectColumnSpec(leftColumn, leftColumnAlias, leftTableAlias);

        // Right table & column
        final Table rightTable = getTable(rightSelectTarget);
        final String rightTableAlias = aliasGenerator.newTableAlias(rightTable);
        final TableMetaData rightTableMetaData = getTableMetaData(rightTable);
        final ColumnMetaData rightColumnMetaData = rightTableMetaData.column(leftColumnMetaData.getJoinColumn());

        // Add right table columns to select
        SelectColumnSpec rightSelectColumnSpec = null;
        final SqlFunctionRegistry sqlFunctionRegistry = litebridgeContext.sqlFunctionRegistry();
        final List<SelectExpression> pendingSelectExpressions = ensurePendingSelectExpressions();

        for (ColumnMetaData columnMetaData : rightTableMetaData.columns()) {
            final Column column = columnMetaData.column();
            final String columnAlias = aliasGenerator.newColumnAlias(column);
            String tableAlias = aliasGenerator.tableAlias(column.table());

            if (tableAlias == null) {
                tableAlias = aliasGenerator.newTableAlias(column.table());
            }

            pendingSelectExpressions.add(sqlFunctionRegistry.select().column().create(column, columnAlias, tableAlias));

            if (columnMetaData.equals(rightColumnMetaData)) {
                // Don't include the column alias for the JOIN clause
                rightSelectColumnSpec = new SelectColumnSpec(column, null, tableAlias);
            }
        }

        return new JoinOnSpec(leftSelectColumnSpec,
                Objects.requireNonNull(rightSelectColumnSpec, "Right JOIN column not selected"));
    }

    private List<JoinOnSpec> processManyToManyJoin(final MappedManyToMany mappedManyToMany, final SelectTarget leftSelectTarget) {
        final JoinOnSpec leftLeftJoinOnSpec = createManyToManyLeftJoinOnSpec(mappedManyToMany, leftSelectTarget);
        return List.of(leftLeftJoinOnSpec, createManyToManyRightJoinOnSpec(mappedManyToMany, leftLeftJoinOnSpec.rightSelectColumnSpec().getColumn().table()));
    }

    private JoinOnSpec createManyToManyLeftJoinOnSpec(final MappedManyToMany mappedManyToMany, final SelectTarget leftSelectTarget) {
        // Left column
        final Table leftTable = getTable(leftSelectTarget);
        final String leftTableAlias = getAlias(leftSelectTarget);
        final OrmTable leftOrmTable = tableRegistry.getOrmTableOrThrow(leftTable);
        final TableMetaData leftTableMetaData = leftOrmTable.getMetaData();

        if (leftTableMetaData.primaryKey().isEmpty()) {
            throw new IllegalArgumentException("Left table " + leftTableMetaData.name() + " does not have a primary key; cannot map many-to-many join: " + mappedManyToMany);
        }

        //TODO: add support for composite primary keys in many-to-many joins
        final ColumnMetaData leftColumnMetaData = leftTableMetaData.primaryKey().getFirst();
        final Column leftColumn = leftColumnMetaData.column();
        final String leftColumnAlias = aliasGenerator.columnAlias(leftColumn);
        final SelectColumnSpec leftSelectColumnSpec = new SelectColumnSpec(leftColumn, leftColumnAlias, leftTableAlias);

        // Join table & column - alias it directly in order to support self-references
        final OrmTable joinOrmTable = mappedManyToMany.joinOrmTable();
        final Table joinTable = joinOrmTable.getMetaData().table();
        final String joinTableAlias = aliasGenerator.newTableAlias(joinTable);
        final Column joinColumn = joinOrmTable.getMetaData().column(mappedManyToMany.joinColumn()).column();
        final String joinColumnAlias = aliasGenerator.newColumnAlias(joinColumn);
        final SelectColumnSpec joinSelectColumnSpec = new SelectColumnSpec(joinColumn, joinColumnAlias, joinTableAlias);

        return new JoinOnSpec(leftSelectColumnSpec, joinSelectColumnSpec);
    }

    private JoinOnSpec createManyToManyRightJoinOnSpec(final MappedManyToMany mappedManyToMany, final Table joinTable) {
        // Join column
        final String joinTableAlias = aliasGenerator.tableAlias(joinTable);
        final TableMetaData joinTableMetaData = getTableMetaData(joinTable);
        final Column joinTableColumn = joinTableMetaData.column(mappedManyToMany.inverseJoinColumn()).column();
        final SelectColumnSpec joinSelectColumnSpec = new SelectColumnSpec(joinTableColumn, null, joinTableAlias);

        // Right column
        final OrmTable rightOrmTable = mappedManyToMany.targetOrmTable().get();
        final TableMetaData rightTableMetaData = rightOrmTable.getMetaData();

        if (rightTableMetaData.primaryKey().isEmpty()) {
            throw new IllegalArgumentException("Right table " + rightTableMetaData.name() + " does not have a primary key; cannot map many-to-many join: " + mappedManyToMany);
        }

        final Table rightTable = rightTableMetaData.table();
        final String rightTableAlias = aliasGenerator.newTableAlias(rightTable);
        //TODO: add support for composite primary keys in many-to-many joins
        final ColumnMetaData rightColumnMetaData = rightTableMetaData.column(mappedManyToMany.inverseJoinColumn());
        final Column rightColumn = rightColumnMetaData.column();
        final String rightColumnAlias = aliasGenerator.newColumnAlias(rightColumn);

        // Add joined table columns to select
        final SqlFunctionRegistry sqlFunctionRegistry = litebridgeContext.sqlFunctionRegistry();
        final List<SelectExpression> pendingSelectExpressions = ensurePendingSelectExpressions();
        SelectColumnSpec rightSelectColumnSpec = null;

        for (ColumnMetaData columnMetaData : rightOrmTable.mappedColumns()) {
            final Column column = columnMetaData.column();
            final String columnAlias = aliasGenerator.newColumnAlias(column);
            String tableAlias = aliasGenerator.tableAlias(column.table());

            if (tableAlias == null) {
                tableAlias = aliasGenerator.newTableAlias(column.table());
            }

            pendingSelectExpressions.add(sqlFunctionRegistry.select().column().create(column, columnAlias, tableAlias));

            if (columnMetaData.equals(rightColumnMetaData)) {
                // Don't include the column alias for the JOIN clause
                rightSelectColumnSpec = new SelectColumnSpec(column, null, tableAlias);
            }
        }

        return new JoinOnSpec(joinSelectColumnSpec, rightSelectColumnSpec);
    }

    private JoinOnSpec processOneToManyReverseJoin(final SelectTarget rightSelectTarget,
                                                   final MappedOneToMany mappedOneToMany,
                                                   final SelectTarget leftSelectTarget,
                                                   final boolean selectAll) {
        // Right table & column
        final Table rightTable = getTable(rightSelectTarget);
        final String rightTableAlias = aliasGenerator.newTableAlias(rightTable);
        final TableMetaData rightTableMetaData = getTableMetaData(rightTable);
        final OrmTable rightOrmTable = tableRegistry.getOrmTableOrThrow(rightTable);
        final ColumnMetaData rightColumnMetaData = rightOrmTable.columnMetaDataForField(mappedOneToMany.mappedByField());

        // Left column
        //TODO: composite primary keys
        final Table leftTable = getTable(leftSelectTarget);
        final OrmTable leftOrmTable = tableRegistry.getOrmTableOrThrow(leftTable);
        final ColumnMetaData leftColumnMetaData = leftOrmTable.getMetaData().primaryKey().getFirst();
        final Column leftColumn = leftColumnMetaData.column();
        final String leftTableAlias = aliasGenerator.tableAlias(leftTable);
        final String leftColumnAlias = aliasGenerator.columnAlias(leftColumn);
        final SelectColumnSpec leftSelectColumnSpec = new SelectColumnSpec(leftColumn, leftColumnAlias, leftTableAlias);

        // Add join table columns to select
        SelectColumnSpec rightSelectColumnSpec = null;
        final SqlFunctionRegistry sqlFunctionRegistry = litebridgeContext.sqlFunctionRegistry();
        final List<SelectExpression> pendingSelectExpressions = ensurePendingSelectExpressions();

        for (ColumnMetaData columnMetaData : rightTableMetaData.columns()) {
            final Column column = columnMetaData.column();
            final String columnAlias = aliasGenerator.newColumnAlias(column);
            String tableAlias = aliasGenerator.tableAlias(column.table());

            if (tableAlias == null) {
                tableAlias = aliasGenerator.newTableAlias(column.table());
            }

            pendingSelectExpressions.add(sqlFunctionRegistry.select().column().create(column, columnAlias, tableAlias));

            if (columnMetaData.equals(rightColumnMetaData)) {
                // Don't include the column alias for the JOIN clause
                rightSelectColumnSpec = new SelectColumnSpec(column, null, tableAlias);
            }
        }

        return new JoinOnSpec(leftSelectColumnSpec,
                Objects.requireNonNull(rightSelectColumnSpec, "Right JOIN column not selected"));
    }

    private List<SelectExpression> ensurePendingSelectExpressions() {
        if (joinSelectExpressions == null) {
            joinSelectExpressions = new ArrayList<>();
        }

        return joinSelectExpressions;
    }

    private record JoinOnSpec(SelectColumnSpec leftSelectColumnSpec,
                              SelectColumnSpec rightSelectColumnSpec) {
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
}
