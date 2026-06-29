package org.litebridgedb.orm.api.select.model;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.commons.ObjectUtils;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.expression.ColumnExpression;
import org.litebridgedb.db.spi.expression.ConvertExpression;
import org.litebridgedb.db.spi.expression.DelegateExpression;
import org.litebridgedb.db.spi.expression.SelectExpression;
import org.litebridgedb.db.spi.query.OrderBy;
import org.litebridgedb.db.spi.query.Select;
import org.litebridgedb.orm.api.select.impl.ProtoExpressionResolver;
import org.litebridgedb.orm.engine.LitebridgeContext;
import org.litebridgedb.orm.expression.ColumnExpressionSpec;
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
import java.util.function.Supplier;
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
    protected @Nullable Supplier<ProtoExpressionResolver> protoExpressionResolver;
    protected List<ExpressionSpec> expressionSpecs = new ArrayList<>();
    protected @Nullable List<JoinSpec> joins;
    protected @Nullable List<ConditionSpec> whereConditions;
    protected @Nullable GroupBySpec groupBy;
    protected @Nullable List<ConditionSpec> havingConditions;
    protected @Nullable List<OrderBySpec> orderBys;
    protected @Nullable LimitSpec limit;
    protected @Nullable Map<Class<?>, String> dtoAliases;

    public SelectSpec(final LitebridgeContext litebridgeContext) {
        this.selectExpressionMapper = litebridgeContext.selectExpressionMapper();
        this.litebridgeContext = litebridgeContext;
    }

    public Table getTable() {
        return ObjectUtils.requireNonNull(table, () -> new IllegalStateException("SelectSpec.table not set"));
    }

    public void setTable(final Table table) {
        this.table = table;
    }

    public @Nullable Supplier<ProtoExpressionResolver> getProtoExpressionResolver() {
        return protoExpressionResolver;
    }

    public void setProtoExpressionResolver(@Nullable final Supplier<ProtoExpressionResolver> protoExpressionResolver) {
        this.protoExpressionResolver = protoExpressionResolver;
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

        this.expressionSpecs = resolveProtoExpressions(expressionSpecs);
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

        // GROUP BY
        final List<SelectExpression> groupByClause;

        if (groupBy != null) {
            groupByClause = convertToSelectExpressions(resolveProtoExpressions(groupBy.expressions(), selectedColumns), false);

            // Validate that the lhs exists in the select statement if a GROUP BY is present
//            groupByClause.forEach(groupBy -> {
//                for (Column column : groupBy.columns()) {
//                    if (selectedColumns.stream().noneMatch(column::equalsIgnoreAlias)) {
//                        throw new IllegalArgumentException("Invalid grouped query: ORDER BY lhs %s must be grouped or aggregated".formatted(column.name()));
//                    }
//                }
//            });
        } else {
            groupByClause = Collections.emptyList();
        }

        final List<OrderBy> orderByClause;

        // ORDER BY
        if (orderBys != null) {
            orderByClause = orderBys.stream()
                    .flatMap(orderBySpec ->
                            convertToSelectExpressions(resolveProtoExpressions(orderBySpec.expressions(), selectedColumns), true).stream()
                                    .map(selectExpression -> new OrderBy(selectExpression, orderBySpec.isAsc())))
                    .toList();
        } else {
            orderByClause = Collections.emptyList();
        }

        return new Select(table,
                selectExpressions,
                joins != null ? joins.stream()
                        .map(JoinSpec::toJoin)
                        .toList() : Collections.emptyList(),
                whereConditions != null ? whereConditions.stream()
                        .map(conditionSpec -> conditionSpec.toCondition(selectExpressionMapper))
                        .toList() : Collections.emptyList(),
                groupByClause,
                havingConditions != null ? havingConditions.stream()
                        .map(conditionSpec -> conditionSpec.toCondition(selectExpressionMapper))
                        .toList() : Collections.emptyList(),
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
        return switch (expressionSpec) {
            case ExpressionSpecArray expressionSpecArray ->
                    convertToSelectExpressionStream(expressionSpecArray.expressions(), useSelectReferences);
            default -> Stream.of(selectExpressionMapper.toSelectExpression(expressionSpec, useSelectReferences));
        };
    }

    private List<ExpressionSpec> resolveProtoExpressions(final List<ExpressionSpec> expressionSpecs) {
        if (protoExpressionResolver == null) {
            throw new IllegalStateException("Cannot resolve proto expressions; no proto-expression resolver set");
        }

        return protoExpressionResolver.get().resolveExpressions(expressionSpecs);
    }

    private List<ExpressionSpec> resolveProtoExpressions(final List<ExpressionSpec> expressionSpecs, final Set<Column> selectedColumns) {
        return resolveProtoExpressions(expressionSpecs).stream()
                // Resolve references to selected columns (to correctly associate aliases)
                .peek(expressionSpec -> {
                    if (expressionSpec instanceof ColumnExpressionSpec columnExpressionSpec) {
                        final Column column = columnExpressionSpec.getColumn();
                        selectedColumns.stream()
                                .filter(column::equalsIgnoreAlias)
                                .findFirst().ifPresent(columnExpressionSpec::setColumn);
                    }
                })
                .toList();
    }
}
