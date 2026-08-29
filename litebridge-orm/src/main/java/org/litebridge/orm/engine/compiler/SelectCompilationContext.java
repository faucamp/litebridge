package org.litebridge.orm.engine.compiler;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.MappedFieldTarget;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.expression.SelectExpression;
import org.litebridge.db.spi.expression.SqlFunctionRegistry;
import org.litebridge.db.spi.query.ConditionGroup;
import org.litebridge.db.spi.query.Join;
import org.litebridge.db.spi.query.Limit;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.db.spi.query.OrderBy;
import org.litebridge.db.spi.query.Select;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.orm.engine.ast.ConditionJoinUsingNode;
import org.litebridge.orm.engine.ast.ConditionNode;
import org.litebridge.orm.engine.ast.ConditionWithIdNode;
import org.litebridge.orm.engine.ast.GroupByNode;
import org.litebridge.orm.engine.ast.JoinNode;
import org.litebridge.orm.engine.ast.LimitNode;
import org.litebridge.orm.engine.ast.OrderByNode;
import org.litebridge.orm.engine.ast.SelectNode;
import org.litebridge.orm.api.select.model.SelectExpressionMapper;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;
import org.litebridge.orm.expression.select.SelectFieldSpec;
import org.litebridge.orm.persistence.MappedOneToMany;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.TableRegistry;
import org.litebridge.tracking.FieldAccessor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

final class SelectCompilationContext extends AbstractCompilationContext {

    private final boolean selectAll;
    private final Table table;
    private final TableMetaData tableMetaData;
    private final @Nullable OrmTable ormTable;
    private final List<SelectExpression> selectExpressions;
    private final SelectExpressionMapper selectExpressionMapper;
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
        this.selectAll = selectNode.isSelectAll();

        if (selectNode.dtoClass() != null) {
            this.ormTable = litebridgeContext.tableRegistry().getOrmTableOrThrow(selectNode.dtoClass());
            this.tableMetaData = ormTable.getMetaData();
            this.table = tableMetaData.toTable();
        } else {
            this.table = litebridgeContext.tableRegistry().getOrCreateSpiTable(Objects.requireNonNull(selectNode.table()));
            this.tableMetaData = litebridgeContext.tableMetaDataCache().ensureTableMetaData(table);
            this.ormTable = null;
        }

        final SqlFunctionRegistry sqlFunctionRegistry = litebridgeContext.sqlFunctionRegistry();

        if (selectAll) {
            // All columns
            final List<ColumnMetaData> columnMetaDatas = tableMetaData.columns();
            final List<String> selectColumns = new ArrayList<>(columnMetaDatas.size());
            this.selectExpressions = new ArrayList<>(columnMetaDatas.size());

            for (ColumnMetaData columnMetaData : columnMetaDatas) {
                selectColumns.add(columnMetaData.name());
                this.selectExpressions.add(sqlFunctionRegistry.select().column().create(columnMetaData.toColumn()));
            }

        } else if (selectNode.columns() != null) {
            // Specific field/column names specified
            final List<String> selectColumns;

            if (ormTable != null) {
                // Translate field names to column names
                selectColumns = Arrays.stream(selectNode.columns())
                        .map(ormTable::columnMetaDataForField)
                        .map(ColumnMetaData::name)
                        .toList();
            } else {
                selectColumns = List.of(selectNode.columns());
            }

            this.selectExpressions = new ArrayList<>(selectColumns.size());

            for (final String selectColumnName : selectColumns) {
                final ColumnMetaData columnMetaData = tableMetaData.column(selectColumnName);
                this.selectExpressions.add(sqlFunctionRegistry.select().column().create(columnMetaData.toColumn()));
            }
        } else {
            // Select expressions
            final ExpressionSpec[] expressionSpecs = Objects.requireNonNull(selectNode.expressions());
            final List<ExpressionSpec> resolvedExpressionSpecs = new ArrayList<>(expressionSpecs.length);

            for (ExpressionSpec expressionSpec : expressionSpecs) {
                resolvedExpressionSpecs.addAll(selectExpressionMapper.resolveProtoExpression(expressionSpec, ormTable, table, ClauseType.SELECT));
            }

            this.selectExpressions = resolvedExpressionSpecs.stream()
                    .map(expressionSpec -> selectExpressionMapper.toSelectExpression(expressionSpec, false))
                    .toList();
        }
    }

    public void addJoin(final JoinNode joinNode) {
        final JoinSpec joinSpec = new JoinSpec(joinNode.type(), joinNode.dtoClass(), joinNode.tableName());
        joinSpec.conditionGroupStack().newRootInstance();

        if (this.joinSpecs == null) {
            this.joinSpecs = new ArrayList<>();
        }

        this.joinSpecs.add(joinSpec);
        currentJoinSpec = joinSpec;
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
                        conditionNode = new ConditionNode(conditionNode, LogicOperator.AND, primaryKeyFieldNames[i], null, conditionWithIdNode.operator(), idList.get(i));
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
        final JoinSpec joinSpec = Objects.requireNonNull(currentJoinSpec, "No current JOIN");
        final ConditionNode conditionNode;

        if (conditionJoinUsingNode.usingColumn() != null) {
            if (ormTable != null) {
                // Get details on the USING column on the local table
                final MappedFieldTarget mappedFieldTarget = ormTable.mappedFieldTargetForField(conditionJoinUsingNode.usingColumn());

                final JoinUsingSpecs joinUsingSpecs = switch (mappedFieldTarget) {
                    case ColumnMetaData usingColumnMetaData ->
                            processOneToManyJoin(joinSpec.dtoClass(), usingColumnMetaData);
                    case MappedOneToMany mappedOneToMany ->
                            processOneToManyReverseJoin(joinSpec.dtoClass(), mappedOneToMany);
                    default ->
                            throw new UnsupportedOperationException("Unsupported mapped field target: " + mappedFieldTarget);
                };

                // Create actual condition
                conditionNode = new ConditionNode(null, conditionJoinUsingNode.logicOperator(), null, joinUsingSpecs.usingSelectColumnSpec(), Operator.EQ, joinUsingSpecs.joinSelectColumnSpec());
            } else {
                throw new UnsupportedOperationException("Not implemented yet");
            }
        } else {
            throw new UnsupportedOperationException("Not implemented yet");
        }

        addJoinCondition(conditionNode);
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
                            .resolveProtoExpression(expressionSpec, ormTable, table, ClauseType.GROUP_BY)
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
                        .map(columnName -> {
                            final ColumnMetaData columnMetaData = ormTable.columnMetaDataForField(columnName);
                            return (SelectExpression) sqlFunctionRegistry.select().reference().create(columnMetaData.toColumn());
                        })
                        .toList();
            } else {
                // Column names
                groupByExpressions = Arrays.stream(columnNames)
                        .map(columnName -> {
                            final Column column = new Column(table, columnName);
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
            orderByExpressions = selectExpressionMapper.resolveProtoExpression(orderByNode.expression(), ormTable, table, ClauseType.ORDER_BY).stream()
                    .map(expressionSpec -> selectExpressionMapper.toSelectExpression(expressionSpec, true))
                    .toList();
        } else {
            // Column/field names
            final String columnName = Objects.requireNonNull(orderByNode.column());

            if (ormTable != null) {
                // DTO field name; translate it to a column
                final ColumnMetaData columnMetaData = ormTable.columnMetaDataForField(columnName);
                orderByExpressions = Collections.singletonList(litebridgeContext.sqlFunctionRegistry().select().reference().create(columnMetaData.toColumn()));
            } else {
                // Column name
                final Column column = new Column(table, columnName);
                orderByExpressions = Collections.singletonList(litebridgeContext.sqlFunctionRegistry().select().reference().create(column));
            }
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
    public List<BindValue> getBindValues() {
        return bindValues != null ? bindValues : Collections.emptyList();
    }

    @Override
    public Select toOperation() {
        final List<Join> joins;

        if (joinSpecs != null) {
            final TableRegistry tableRegistry = litebridgeContext.tableRegistry();

            joins = joinSpecs.stream()
                    .map(joinSpec -> {
                        final Table joinTable;

                        if (joinSpec.dtoClass() != null) {
                            joinTable = tableRegistry.getOrmTableOrThrow(joinSpec.dtoClass()).getMetaData().toTable();
                        } else {
                            joinTable = tableRegistry.getOrCreateSpiTable(Objects.requireNonNull(joinSpec.tableName()));
                        }

                        final ConditionGroup joinConditionGroup = toConditionGroup(joinSpec.conditionGroupStack().current(), ormTable, joinTable);
                        return new Join(joinTable, joinConditionGroup);
                    })
                    .toList();
        } else {
            joins = null;
        }

        final ConditionGroup whereConditionGroup = where != null ? toConditionGroup(where.current(), ormTable, table) : null;
        final ConditionGroup havingConditionGroup = having != null ? toConditionGroup(having.current(), ormTable, table) : null;

        return new Select(table,
                selectExpressions,
                joins,
                whereConditionGroup,
                groupBy,
                havingConditionGroup,
                orderBys,
                limit);
    }

    private JoinUsingSpecs processOneToManyJoin(final Class<?> joinDtoClass, final ColumnMetaData usingColumnMetaData) {
        // Local column
        final SelectColumnSpec usingSelectColumnSpec = new SelectColumnSpec(usingColumnMetaData.toColumn());

        // Join table & column
        final OrmTable joinOrmTable = litebridgeContext.tableRegistry().getOrmTableOrThrow(Objects.requireNonNull(joinDtoClass));
        final TableMetaData joinTableMetaData = joinOrmTable.getMetaData();
        final ColumnMetaData joinColumnMetadata = joinTableMetaData.column(usingColumnMetaData.getJoinColumn());
        final SelectColumnSpec joinSelectColumnSpec = new SelectColumnSpec(joinColumnMetadata.toColumn());

        // Add join table columns to select
        if (selectAll) {
            final SqlFunctionRegistry sqlFunctionRegistry = litebridgeContext.sqlFunctionRegistry();

            for (ColumnMetaData columnMetaData : joinTableMetaData.columns()) {
                this.selectExpressions.add(sqlFunctionRegistry.select().column().create(columnMetaData.toColumn()));
            }
        }

        return new JoinUsingSpecs(usingSelectColumnSpec, joinSelectColumnSpec);
    }

    private JoinUsingSpecs processOneToManyReverseJoin(final Class<?> joinDtoClass, final MappedOneToMany mappedOneToMany) {
        // Join table & column
        final OrmTable joinOrmTable = litebridgeContext.tableRegistry().getOrmTableOrThrow(Objects.requireNonNull(joinDtoClass));
        final ColumnMetaData joinColumnMetaData = joinOrmTable.columnMetaDataForField(mappedOneToMany.mappedByField());
        final SelectColumnSpec joinSelectColumnSpec = new SelectColumnSpec(joinColumnMetaData.toColumn());

        // Local column
        //TODO: composite primary keys
        final ColumnMetaData usingColumnMetaData = ormTable.getMetaData().primaryKey().getFirst();
        final SelectColumnSpec usingSelectColumnSpec = new SelectColumnSpec(usingColumnMetaData.toColumn());

        // Add join table columns to select
        if (selectAll) {
            final SqlFunctionRegistry sqlFunctionRegistry = litebridgeContext.sqlFunctionRegistry();

            for (ColumnMetaData columnMetaData : joinOrmTable.getMetaData().columns()) {
                this.selectExpressions.add(sqlFunctionRegistry.select().column().create(columnMetaData.toColumn()));
            }
        }

        return new JoinUsingSpecs(usingSelectColumnSpec, joinSelectColumnSpec);
    }

    private record JoinUsingSpecs(SelectColumnSpec usingSelectColumnSpec, SelectColumnSpec joinSelectColumnSpec) {
    }
}
