package org.litebridge.orm.engine.compiler;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.expression.SelectExpression;
import org.litebridge.db.spi.expression.SqlFunctionRegistry;
import org.litebridge.db.spi.query.ConditionGroup;
import org.litebridge.db.spi.query.Join;
import org.litebridge.db.spi.query.Limit;
import org.litebridge.db.spi.query.OrderBy;
import org.litebridge.db.spi.query.Select;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.orm.api.select.ast.ConditionNode;
import org.litebridge.orm.api.select.ast.GroupByNode;
import org.litebridge.orm.api.select.ast.JoinNode;
import org.litebridge.orm.api.select.ast.LimitNode;
import org.litebridge.orm.api.select.ast.OrderByNode;
import org.litebridge.orm.api.select.ast.SelectNode;
import org.litebridge.orm.api.select.model.SelectExpressionMapper;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.TableRegistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class SelectCompilationContext extends AbstractCompilationContext {

    private final Table table;
    private final TableMetaData tableMetaData;
    private final @Nullable OrmTable ormTable;
    private final List<ColumnMetaData> columnMetaDataList = new ArrayList<>();
    private final List<SelectExpression> selectExpressions;
    private final SelectExpressionMapper selectExpressionMapper;
    private @Nullable List<JoinSpec> joinSpecs;
    private @Nullable JoinSpec currentJoinSpec;
    private @Nullable ConditionGroupSpecStack where;
    private @Nullable List<SelectExpression> groupBy;
    private @Nullable ConditionGroupSpecStack having;
    private @Nullable List<OrderBy> orderBys;
    private @Nullable Limit limit;

    public SelectCompilationContext(final SelectNode selectNode,
                                    final LitebridgeContext litebridgeContext) {
        super(litebridgeContext);
        this.selectExpressionMapper = litebridgeContext.selectExpressionMapper();

        if (selectNode.dtoClass() != null) {
            this.ormTable = litebridgeContext.tableRegistry().getTableOrThrow(selectNode.dtoClass());
            this.tableMetaData = ormTable.getMetaData();
            this.table = tableMetaData.toTable();
        } else {
            this.table = litebridgeContext.tableRegistry().getOrCreateSpiTable(Objects.requireNonNull(selectNode.table()));
            this.tableMetaData = litebridgeContext.tableMetaDataCache().ensureTableMetaData(table);
            this.ormTable = null;
        }

        final SqlFunctionRegistry sqlFunctionRegistry = litebridgeContext.sqlFunctionRegistry();

        if (selectNode.isSelectAll()) {
            // All columns
            final List<ColumnMetaData> columnMetaDatas = tableMetaData.columns();
            final List<String> selectColumns = new ArrayList<>(columnMetaDatas.size());
            this.selectExpressions = new ArrayList<>(columnMetaDatas.size());

            for (ColumnMetaData columnMetaData : columnMetaDatas) {
                this.columnMetaDataList.add(columnMetaData);
                selectColumns.add(columnMetaData.name());
                this.selectExpressions.add(sqlFunctionRegistry.select().column().create(columnMetaData.toColumn()));
            }

        } else if (selectNode.columns() != null) {
            // Specific field/column names specified
            final List<String> selectColumns;

            if (ormTable != null) {
                // Translate field names to column names
                selectColumns = Arrays.stream(selectNode.columns())
                        .map(ormTable::getColumnForFieldName)
                        .map(ColumnMetaData::name)
                        .toList();
            } else {
                selectColumns = List.of(selectNode.columns());
            }

            this.selectExpressions = new ArrayList<>(selectColumns.size());

            for (final String selectColumnName : selectColumns) {
                final ColumnMetaData columnMetaData = tableMetaData.column(selectColumnName);
                this.columnMetaDataList.add(columnMetaData);
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
        Objects.requireNonNull(currentJoinSpec, "No current JOIN")
                .conditionGroupStack().current()
                .newCondition(conditionNode.logicOperator(),
                        conditionNode.lhsColumn(),
                        conditionNode.lhsExpression(),
                        conditionNode.operator(),
                        conditionNode.rhs());
    }

    public void addWhereCondition(final ConditionNode conditionNode) {
        ensureWhereConditionGroupStack().current()
                .newCondition(conditionNode.logicOperator(),
                        conditionNode.lhsColumn(),
                        conditionNode.lhsExpression(),
                        conditionNode.operator(),
                        conditionNode.rhs());
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
                            final ColumnMetaData columnMetaData = ormTable.getColumnForFieldName(columnName);
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
                final ColumnMetaData columnMetaData = ormTable.getColumnForFieldName(columnName);
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
                            joinTable = tableRegistry.getTableOrThrow(joinSpec.dtoClass()).getMetaData().toTable();
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
}
