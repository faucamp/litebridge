package org.litebridge.orm.api.select.model;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.ObjectUtils;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.expression.ColumnExpression;
import org.litebridge.db.spi.expression.DelegateExpression;
import org.litebridge.db.spi.expression.SelectExpression;
import org.litebridge.db.spi.query.ConditionGroup;
import org.litebridge.db.spi.query.Join;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.OrderBy;
import org.litebridge.db.spi.query.Select;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ColumnExpressionSpec;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.intent.ExpressionSpecArray;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Base specification for constructing a SQL SELECT statement.
 * <p>
 * This class encapsulates table, column, join, condition, order by,
 * and limit specifications for building a query.
 * <p>
 * Its subclasses {@link org.litebridge.orm.api.dto.DtoSelectSpec} and {@link org.litebridge.orm.api.sql.SqlSelectSpec}
 * specialise in dealing with DTOs and SQL-specific constructs, respectively.
 */
public abstract class SelectSpec {

    protected @Nullable SelectExpressionMapper selectExpressionMapper;
    protected final LitebridgeContext litebridgeContext;

    protected @Nullable Table table;
    protected List<ExpressionSpec> expressionSpecs = new ArrayList<>();
    protected @Nullable List<JoinSpec> joins;
    protected @Nullable ConditionGroupSpec whereConditions;
    protected @Nullable GroupBySpec groupBy;
    protected @Nullable ConditionGroupSpec havingConditions;
    protected @Nullable List<OrderBySpec> orderBys;
    protected @Nullable LimitSpec limit;
    private final Deque<ConditionGroupSpec> whereConditionGroupStack = new ArrayDeque<>();
    private final Deque<ConditionGroupSpec> havingConditionGroupStack = new ArrayDeque<>();

    protected SelectSpec(final LitebridgeContext litebridgeContext) {
        this.litebridgeContext = litebridgeContext;
    }

    public Table getTable() {
        return ObjectUtils.requireNonNull(table, () -> new IllegalStateException("SelectSpec.table not set"));
    }

    public void setTable(final Table table) {
        this.table = table;
    }

    public void setProtoExpressionResolver(final ProtoExpressionResolver protoExpressionResolver) {
        this.selectExpressionMapper = new SelectExpressionMapper(litebridgeContext.sqlFunctionRegistry(), protoExpressionResolver);
    }

    public LitebridgeContext getLitebridgeContext() {
        return litebridgeContext;
    }

    public SelectExpressionMapper selectExpressionMapper() {
        return Objects.requireNonNull(selectExpressionMapper, "SelectExpressionMapper not set");
    }

    public boolean isTableSet() {
        return table != null;
    }

    public boolean isSelectExpressionMapperSet() {
        return selectExpressionMapper != null;
    }

    public SelectExpressionMapper getSelectExpressionMapper() {
        return selectExpressionMapper;
    }

    public List<ExpressionSpec> getExpressions() {
        return expressionSpecs;
    }

    public void setExpressions(final List<ExpressionSpec> expressionSpecs) {
        if (expressionSpecs instanceof ArrayList) {
            this.expressionSpecs = expressionSpecs;
        } else {
            this.expressionSpecs = new ArrayList<>(expressionSpecs);
        }
    }

    public void addExpressions(final List<? extends ExpressionSpec> expressions) {
        this.expressionSpecs.addAll(expressions);
    }

    public @Nullable List<JoinSpec> getJoins() {
        return joins;
    }

    public void addJoin(final JoinSpec join) {
        if (joins == null) {
            joins = new ArrayList<>();
        }
        joins.add(join);
    }

    public ConditionGroupSpec currentWhereConditionGroupSpec() {
        if (whereConditionGroupStack.isEmpty()) {
            return ensureWhereConditions();
        }

        return whereConditionGroupStack.peek();
    }

    public ConditionGroupSpec pushWhereConditionGroup(final LogicOperator logicOperator) {
        final ConditionGroupSpec subgroup = currentWhereConditionGroupSpec().newSubgroup(logicOperator).conditionGroupSpec();
        whereConditionGroupStack.push(subgroup);
        return subgroup;
    }

    public void popWhereConditionGroup() {
        whereConditionGroupStack.pop();
    }

    public ConditionGroupSpec currentHavingConditionGroupSpec() {
        if (havingConditionGroupStack.isEmpty()) {
            return ensureHavingConditions();
        }

        return havingConditionGroupStack.peek();
    }

    public ConditionGroupSpec pushHavingConditionGroup(final LogicOperator logicOperator) {
        final ConditionGroupSpec subgroup = ensureHavingConditions().newSubgroup(logicOperator).conditionGroupSpec();
        havingConditionGroupStack.push(subgroup);
        return subgroup;
    }

    public void popHavingConditionGroup() {
        havingConditionGroupStack.pop();
    }

    public void addWhereCondition(final ExpressionSpec condition) {
        currentWhereConditionGroupSpec().newCondition(LogicOperator.NOOP, condition);
    }

    public void addHavingCondition(final ExpressionSpec condition) {
        currentHavingConditionGroupSpec().newCondition(LogicOperator.NOOP, condition);
    }

    public void addOrderBy(final ExpressionSpec expression, final boolean ascending) {
        final OrderBySpec orderBySpec = newOrderBy(expression);
        orderBySpec.setAsc(ascending);
    }

    public void setGroupBy(final List<ExpressionSpec> expressions) {
        this.groupBy = new GroupBySpec(expressions);
    }

    public @Nullable GroupBySpec getGroupBy() {
        return groupBy;
    }

    public void setGroupBy(@Nullable final GroupBySpec groupBy) {
        this.groupBy = groupBy;
    }

    public @Nullable List<OrderBySpec> getOrderBys() {
        return orderBys;
    }

    public OrderBySpec newOrderBy(final ExpressionSpec... expressions) {
        Objects.requireNonNull(expressions, "No LHS expressions specified for ORDER BY");
        return newOrderBy(Arrays.stream(expressions).toList());
    }

    public OrderBySpec newOrderBy(final List<ExpressionSpec> expressions) {
        if (this.orderBys == null) {
            orderBys = new ArrayList<>();
        }

        final OrderBySpec orderBySpec = new OrderBySpec(expressions);
        orderBys.add(orderBySpec);
        return orderBySpec;
    }

    public @Nullable LimitSpec getLimit() {
        return limit;
    }

    public LimitSpec ensureLimit() {
        if (this.limit == null) {
            limit = new LimitSpec();
        }

        return limit;
    }

    public Select toSelect() {
        if (table == null) {
            throw new IllegalStateException("Table not specified");
        }

        // SELECT
        this.expressionSpecs = Objects.requireNonNull(selectExpressionMapper).resolveProtoExpressions(expressionSpecs, ClauseType.SELECT);
        final List<SelectExpression> selectExpressions = convertToSelectExpressions(expressionSpecs, false);
        final Set<Column> selectedColumns = selectExpressions.stream()
                .map(selectExpression -> {
                    if (selectExpression instanceof DelegateExpression delegateExpression) {
                        SelectExpression nestedExpression = delegateExpression.target();

                        while (nestedExpression instanceof DelegateExpression nested) {
                            nestedExpression = nested.target();
                        }

                        return nestedExpression;
                    } else {
                        return selectExpression;
                    }
                })
                .filter(selectExpression -> selectExpression instanceof ColumnExpression)
                .map(ColumnExpression.class::cast)
                .map(ColumnExpression::column)
                .collect(Collectors.toSet());

        // JOIN
        final List<Join> joinClause = new ArrayList<>();
        final List<Table> currentTables = new ArrayList<>();
        currentTables.add(table);

        if (joins != null) {
            for (final JoinSpec joinSpec : joins) {
                joinClause.add(joinSpec.toJoin(currentTables));
                currentTables.add(joinSpec.table());
            }
        }

        final Set<Table> selectedTables = Stream.concat(selectedColumns.stream().map(Column::table),
                        joinClause.stream().map(join -> join.table()))
                .collect(Collectors.toSet());

        // WHERE
        final Optional<ConditionGroup> whereClause = whereConditions != null ? Optional.of(whereConditions.toConditionGroup(selectExpressionMapper, selectedTables)) : Optional.empty();

        // GROUP BY
        final List<SelectExpression> groupByClause;

        if (groupBy != null) {
            groupByClause = convertToSelectExpressions(resolveProtoExpressions(groupBy.expressions(), ClauseType.GROUP_BY, selectedColumns), false);

            // Validate that the column exists in the select statement if a GROUP BY is present
//            groupByClause.forEach(groupBy -> {
//                for (Column column : groupBy.columns()) {
//                    if (selectedColumns.stream().noneMatch(column::equalsIgnoreAlias)) {
//                        throw new IllegalArgumentException("Invalid grouped query: ORDER BY column %s must be grouped or aggregated".formatted(column.name()));
//                    }
//                }
//            });
        } else {
            groupByClause = Collections.emptyList();
        }

        final Optional<ConditionGroup> havingClause = havingConditions != null ? Optional.of(havingConditions.toConditionGroup(selectExpressionMapper, selectedTables)) : Optional.empty();

        final List<OrderBy> orderByClause;

        // ORDER BY
        if (orderBys != null) {
            orderByClause = orderBys.stream()
                    .flatMap(orderBySpec ->
                            convertToSelectExpressions(resolveProtoExpressions(orderBySpec.expressions(), ClauseType.ORDER_BY, selectedColumns), true).stream()
                                    .map(selectExpression -> new OrderBy(selectExpression, orderBySpec.isAsc())))
                    .toList();
        } else {
            orderByClause = Collections.emptyList();
        }

        return new Select(table,
                selectExpressions,
                joinClause,
                whereClause,
                groupByClause,
                havingClause,
                orderByClause,
                limit != null ? limit.toLimit() : Optional.empty());
    }

    private List<SelectExpression> convertToSelectExpressions(final List<ExpressionSpec> expressionSpecs, final boolean useSelectReferences) {
        return convertToSelectExpressions(expressionSpecs.stream(), useSelectReferences);
    }

    private List<SelectExpression> convertToSelectExpressions(final Stream<ExpressionSpec> expressionSpecs, final boolean useSelectReferences) {
        return convertToSelectExpressionStream(expressionSpecs, useSelectReferences)
                .toList();
    }

    public Stream<SelectExpression> convertToSelectExpressionStream(final ExpressionSpec[] expressionSpecs, final boolean useSelectReferences) {
        return convertToSelectExpressionStream(Arrays.stream(expressionSpecs), useSelectReferences);
    }

    private Stream<SelectExpression> convertToSelectExpressionStream(final Stream<ExpressionSpec> expressionSpecs, final boolean useSelectReferences) {
        return expressionSpecs.flatMap(expressionSpec -> convertToSelectExpressionStream(expressionSpec, useSelectReferences));
    }

    private Stream<? extends SelectExpression> convertToSelectExpressionStream(final ExpressionSpec expressionSpec, final boolean useSelectReferences) {
        Objects.requireNonNull(selectExpressionMapper);
        return switch (expressionSpec) {
            case ExpressionSpecArray expressionSpecArray ->
                    convertToSelectExpressionStream(expressionSpecArray.expressions(), useSelectReferences);
            default -> Stream.of(selectExpressionMapper.toSelectExpression(expressionSpec, useSelectReferences));
        };
    }

    private List<ExpressionSpec> resolveProtoExpressions(final List<ExpressionSpec> expressionSpecs, final ClauseType clause, final Set<Column> selectedColumns) {
        return Objects.requireNonNull(selectExpressionMapper).resolveProtoExpressions(expressionSpecs, clause).stream()
                // Resolve references to selected columns (to correctly associate aliases)
                .peek(expressionSpec -> {
                    if (expressionSpec instanceof ColumnExpressionSpec columnExpressionSpec) {
                        final Column column = columnExpressionSpec.getColumn();

                        for (Column selectedColumn : selectedColumns) {
                            if (column.equalsIgnoreAlias(selectedColumn)) {
                                // Overwrite with the selected column to copy the alias
                                columnExpressionSpec.setColumn(selectedColumn);
                            }
                        }

                        // No direct column match; Copy the table alias
                        if (column.table().alias() == null && getTable().equalsIgnoreAlias(column.table())) {
                            final Column replacementColumn = new Column(getTable(), column.name());
                            columnExpressionSpec.setColumn(replacementColumn);
                        }
                    }
                })
                .toList();
    }

    private ConditionGroupSpec ensureWhereConditions() {
        if (whereConditions == null) {
            whereConditions = new ConditionGroupSpec();
        }

        return whereConditions;
    }

    private ConditionGroupSpec ensureHavingConditions() {
        if (havingConditions == null) {
            havingConditions = new ConditionGroupSpec();
        }

        return havingConditions;
    }
}
