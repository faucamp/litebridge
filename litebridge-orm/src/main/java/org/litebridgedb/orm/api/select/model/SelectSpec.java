package org.litebridgedb.orm.api.select.model;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.commons.ObjectUtils;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.expression.SqlFunctionRegistry;
import org.litebridgedb.db.spi.expression.ColumnExpression;
import org.litebridgedb.db.spi.query.OrderBy;
import org.litebridgedb.db.spi.query.Select;
import org.litebridgedb.db.spi.expression.SelectExpression;
import org.litebridgedb.orm.api.select.impl.LitebridgeContext;
import org.litebridgedb.orm.expression.ExpressionSpec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Base specification for constructing a SQL SELECT statement.
 * <p>
 * This class encapsulates table, column, join, condition, order by,
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
    protected @Nullable List<OrderBySpec> orderBys;
    protected @Nullable LimitSpec limit;
    protected @Nullable Map<Class<?>, String> dtoAliases;

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
        conditionSpec.setColumn(column);
        whereConditions.add(conditionSpec);
        return conditionSpec;
    }

    public @Nullable List<OrderBySpec> getOrderBys() {
        return orderBys;
    }

    public void setOrderBys(@Nullable final List<OrderBySpec> orderBys) {
        this.orderBys = orderBys;
    }

    public OrderBySpec newOrderBy(final String... columns) {
        Objects.requireNonNull(columns, "No column(s) specified for ORDER BY");

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
        final List<SelectExpression> selectExpressions;

        if (!expressionSpecs.isEmpty()) {
            selectExpressions = expressionSpecs.stream()
                    .map(selectExpressionMapper::toSelectExpression)
                    .toList();
        } else {
            selectExpressions = Collections.emptyList();
        }

        return new Select(table,
                Collections.unmodifiableList(selectExpressions),
                joins != null ? joins.stream()
                        .map(JoinSpec::toJoin)
                        .toList() : Collections.emptyList(),
                orderBys != null ? orderBys.stream()
                        // Resolves order-by expressions from select list or synthesizes new ones
                        .flatMap(orderBySpec -> Arrays.stream(orderBySpec.columns())
                                .map(columnName -> selectExpressions.stream()
                                        .filter(expression -> expression instanceof ColumnExpression)
                                        .map(expression -> ((ColumnExpression) expression).column())
                                        .filter(column -> Objects.equals(column.name(), columnName))
                                        .findFirst()
                                        // Column not specified in select list
                                        .orElseGet(() -> new Column(table, columnName)))
                                .map(column -> new OrderBy(column, orderBySpec.isAsc())))
                        .toList() : Collections.emptyList(),
                whereConditions != null ? whereConditions.stream()
                        .map(conditionSpec -> conditionSpec.toCondition(selectExpressionMapper))
                        .toList() : Collections.emptyList(),
                limit != null ? limit.toLimit() : Optional.empty());
    }
}
