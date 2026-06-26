package org.litebridgedb.orm.api.select.model;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.commons.ObjectUtils;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.expression.ColumnExpression;
import org.litebridgedb.db.spi.expression.ColumnExpressionImpl;
import org.litebridgedb.db.spi.expression.ConvertExpression;
import org.litebridgedb.db.spi.expression.SelectExpression;
import org.litebridgedb.db.spi.query.GroupBy;
import org.litebridgedb.db.spi.query.OrderBy;
import org.litebridgedb.db.spi.query.Select;
import org.litebridgedb.orm.api.select.impl.LitebridgeContext;
import org.litebridgedb.orm.expression.ExpressionSpec;
import org.litebridgedb.orm.expression.intent.ExpressionSpecArray;
import org.litebridgedb.orm.expression.select.SelectColumnSpec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
 * This class encapsulates table, lhs, join, condition, order by,
 * and limit specifications for building a query.
 * <p>
 * Its subclasses {@link org.litebridgedb.orm.api.dto.DtoSelectSpec} and {@link org.litebridgedb.orm.api.sql.SqlSelectSpec}
 * specialise in dealing with DTOs and SQL-specific constructs, respectively.
 */
public abstract class SelectSpec {

    protected final SelectExpressionMapper selectExpressionMapper;
    protected final LitebridgeContext litebridgeContext;

    protected @Nullable Table table;
    protected List<ExpressionSpec> expressionSpecs = new ArrayList<>();
    protected @Nullable List<JoinSpec> joins;
    protected @Nullable List<ConditionSpec> whereConditions;
    protected @Nullable GroupBySpec groupBy;
    protected @Nullable List<ConditionSpec> havingConditions;
    protected @Nullable List<OrderBySpec> orderBys;
    protected @Nullable LimitSpec limit;
    protected @Nullable Map<Class<?>, String> dtoAliases;
    private Map<List<SelectExpression>, Class<?>> expressionTypeOverrides;

    public SelectSpec(final LitebridgeContext litebridgeContext) {
        this.selectExpressionMapper = new SelectExpressionMapper(litebridgeContext.sqlFunctionRegistry());
        this.litebridgeContext = litebridgeContext;
    }

    public Table getTable() {
        return ObjectUtils.requireNonNull(table, () -> new IllegalStateException("SelectSpec.table not set"));
    }

    public void setTable(final Table table) {
        this.table = table;
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

    public void setJoins(@Nullable final List<JoinSpec> joins) {
        this.joins = joins;
    }

    public @Nullable List<ConditionSpec> getWhereConditions() {
        return whereConditions;
    }

    public void setWhereConditions(@Nullable final List<ConditionSpec> whereConditions) {
        this.whereConditions = whereConditions;
    }

    public ConditionSpec newWhereCondition(final Column column) {
        if (this.whereConditions == null) {
            whereConditions = new ArrayList<>();
        }

        final ConditionSpec conditionSpec = new ConditionSpec();
        conditionSpec.setLhs(new SelectColumnSpec(column));
        whereConditions.add(conditionSpec);
        return conditionSpec;
    }

    public ConditionSpec newHavingCondition(final ExpressionSpec expressionSpec) {
        if (this.havingConditions == null) {
            havingConditions = new ArrayList<>();
        }

        final ConditionSpec conditionSpec = new ConditionSpec();
        conditionSpec.setLhs(expressionSpec);
        havingConditions.add(conditionSpec);
        return conditionSpec;
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

    public void setOrderBys(@Nullable final List<OrderBySpec> orderBys) {
        this.orderBys = orderBys;
    }

    public OrderBySpec newOrderBy(final String... columns) {
        Objects.requireNonNull(columns, "No lhs(s) specified for ORDER BY");

        if (this.orderBys == null) {
            orderBys = new ArrayList<>();
        }

        final OrderBySpec orderBySpec = new OrderBySpec(columns);
        orderBys.add(orderBySpec);
        return orderBySpec;
    }

    public @Nullable LimitSpec getLimit() {
        return limit;
    }

    public void setLimit(final LimitSpec limit) {
        this.limit = limit;
    }

    public LimitSpec ensureLimit() {
        if (this.limit == null) {
            limit = new LimitSpec();
        }

        return limit;
    }

    public void setDtoAlias(Class<?> dtoClass, String alias) {
        if (dtoAliases == null) {
            dtoAliases = new HashMap<>();
        }

        dtoAliases.put(dtoClass, alias);
    }

    public @Nullable String getDtoAlias(Class<?> dtoClass) {
        if (dtoAliases != null) {
            return dtoAliases.get(dtoClass);
        } else {
            return null;
        }
    }

    public Select toSelect() {
        if (table == null) {
            throw new IllegalStateException("Table not specified");
        }

        final List<ExpressionSpec> expressionSpecs = getExpressions();
        final List<SelectExpression> selectExpressions = convertToSelectExpressions(expressionSpecs);

        final Select select = new Select(table,
                Collections.unmodifiableList(selectExpressions),
                joins != null ? joins.stream()
                        .map(JoinSpec::toJoin)
                        .toList() : Collections.emptyList(),
                whereConditions != null ? whereConditions.stream()
                        .map(conditionSpec -> conditionSpec.toCondition(selectExpressionMapper))
                        .toList() : Collections.emptyList(),
                groupBy != null ? Optional.of(new GroupBy(Arrays.stream(groupBy.columns())
                        .map(columnName -> selectExpressions.stream()
                                .filter(expression -> expression instanceof ColumnExpressionImpl)
                                .map(expression -> ((ColumnExpression) expression).column())
                                .filter(column -> Objects.equals(column.name(), columnName))
                                .findFirst()
                                // Column not specified in select list
                                .orElseGet(() -> new Column(table, columnName)))
                        .toList())) : Optional.empty(),
                havingConditions != null ? havingConditions.stream()
                        .map(conditionSpec -> conditionSpec.toCondition(selectExpressionMapper))
                        .toList() : Collections.emptyList(),
                orderBys != null ? orderBys.stream()
                        // Resolves order-by expressions from select list or synthesizes new ones
                        .flatMap(orderBySpec -> Arrays.stream(orderBySpec.columns())
                                .map(columnName -> selectExpressions.stream()
                                        .filter(expression -> expression instanceof ColumnExpressionImpl)
                                        .map(expression -> ((ColumnExpression) expression).column())
                                        .filter(column -> Objects.equals(column.name(), columnName))
                                        .findFirst()
                                        // Column not specified in select list
                                        .orElseGet(() -> new Column(table, columnName)))
                                .map(column -> new OrderBy(column, orderBySpec.isAsc())))
                        .toList() : Collections.emptyList(),
                limit != null ? limit.toLimit() : Optional.empty());

        // Validate that the lhs exists in the select statement if a GROUP BY is present
        select.groupBy().ifPresent(groupBy -> {
            final Set<Column> selectedColumns = selectExpressions.stream()
                    .map(expression -> expression instanceof ConvertExpression convertExpression ? convertExpression.target() : expression)
                    .map(expression -> expression instanceof ColumnExpression ? ((ColumnExpression) expression).column() : null)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            for (Column column : groupBy.columns()) {
                if (selectedColumns.stream().noneMatch(column::equalsIgnoreAlias)) {
                    throw new IllegalArgumentException("Invalid grouped query: ORDER BY lhs %s must be grouped or aggregated".formatted(column.name()));
                }
            }
        });

        return select;
    }

    public Optional<Map<List<SelectExpression>, Class<?>>> expressionTypeOverrides() {
        return Optional.ofNullable(expressionTypeOverrides);
    }

    private List<SelectExpression> convertToSelectExpressions(final ExpressionSpec[] expressionSpecs) {
        return convertToSelectExpressions(Arrays.stream(expressionSpecs));
    }

    private List<SelectExpression> convertToSelectExpressions(final List<ExpressionSpec> expressionSpecs) {
        return convertToSelectExpressions(expressionSpecs.stream());
    }

    private List<SelectExpression> convertToSelectExpressions(final Stream<ExpressionSpec> expressionSpecs) {
        return convertToSelectExpressionStream(expressionSpecs)
                .toList();
    }

    private Stream<SelectExpression> convertToSelectExpressionStream(final ExpressionSpec[] expressionSpecs) {
        return convertToSelectExpressionStream(Arrays.stream(expressionSpecs));
    }

    private Stream<SelectExpression> convertToSelectExpressionStream(final Stream<ExpressionSpec> expressionSpecs) {
        return expressionSpecs.flatMap(this::convertToSelectExpressionStream);
    }

    private Stream<? extends SelectExpression> convertToSelectExpressionStream(final ExpressionSpec expressionSpec) {
        return switch (expressionSpec) {
            case ExpressionSpecArray expressionSpecArray ->
                    convertToSelectExpressionStream(expressionSpecArray.expressions());
            default -> Stream.of(selectExpressionMapper.toSelectExpression(expressionSpec));
        };
    }

    private void addTypeOverride(final List<SelectExpression> expressions, Class<?> typeOverride) {
        if (expressionTypeOverrides == null) {
            expressionTypeOverrides = new HashMap<>();
        }

        expressionTypeOverrides.put(expressions, typeOverride);
    }
}
