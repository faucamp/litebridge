package org.litebridge.orm.api.select.model;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.ObjectUtils;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.query.Select;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Specification for constructing a SQL SELECT statement.
 * <p>
 * This class encapsulates table, column, join, condition, order by,
 * and limit specifications for building a query.
 */
public class SelectSpec {

    @Nullable
    private Table table;
    @Nullable
    private List<Column> columns;
    @Nullable
    private List<JoinSpec> joins;
    @Nullable
    private List<ConditionSpec> whereConditions;
    @Nullable
    private List<OrderBySpec> orderBys;
    @Nullable
    private LimitSpec limit;
    @Nullable
    private Map<Class<?>, String> dtoAliases;

    public @Nullable Table getTable() {
        return table;
    }

    public void setTable(final Table table) {
        this.table = table;
    }

    public @Nullable List<Column> getColumns() {
        return columns;
    }

    public void setColumns(final List<Column> columns) {
        setColumns(columns.stream());
    }

    public void addColumns(final Collection<? extends Column> columns) {
        if (this.columns == null) {
            this.columns = new ArrayList<>();
        } else if (!(columns instanceof ArrayList)) {
            this.columns = new ArrayList<>(this.columns);
        }

        this.columns.addAll(sanitise(((List<Column>) columns).stream()));
    }

    public void setColumns(final Stream<Column> columns) {
        this.columns = sanitise(columns);
    }

    public @Nullable List<JoinSpec> getJoins() {
        return joins;
    }

    public void setJoins(@Nullable final List<JoinSpec> joins) {
        this.joins = joins;
    }

    public JoinSpec newJoinSpec(final String table) {
        return newJoinSpec("", table);
    }

    public JoinSpec newJoinSpec(final String schema, final String table) {
        if (this.joins == null) {
            joins = new ArrayList<>();
        }

        final JoinSpec joinSpec = new JoinSpec(schema, table);
        joins.add(joinSpec);
        return joinSpec;
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
        conditionSpec.setColumn(sanitise(column));
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
        ObjectUtils.requireNonNull(columns, "No column(s) specified for ORDER BY");

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

        return new Select(table,
                columns != null ? Collections.unmodifiableList(columns) : Collections.emptyList(),
                joins != null ? joins.stream()
                        .map(JoinSpec::toJoin)
                        .toList() : Collections.emptyList(),
                orderBys != null ? orderBys.stream()
                        .flatMap(orderBySpec -> orderBySpec.toOrderBys().stream())
                        .toList() : Collections.emptyList(),
                whereConditions != null ? whereConditions.stream()
                        .map(ConditionSpec::toCondition)
                        .toList() : Collections.emptyList(),
                limit != null ? limit.toLimit() : Optional.empty());
    }

    private List<Column> sanitise(final Stream<Column> columns) {
        // Ensure just one instance of the same table is used
        return columns
                .map(this::sanitise)
                .toList();
    }

    private Column sanitise(final Column column) {
        if (column.table() != table
                && column.table().alias() == null
                && Objects.equals(column.table().schema(), table.schema())
                && Objects.equals(column.table().name(), table.name())) {
            return new Column(table, column.name(), column.alias());
        } else {
            return column;
        }
    }
}
