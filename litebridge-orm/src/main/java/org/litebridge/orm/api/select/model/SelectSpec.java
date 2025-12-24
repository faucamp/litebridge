package org.litebridge.orm.api.select.model;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.ObjectUtils;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.query.Select;
import org.litebridge.db.spi.query.SelectField;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class SelectSpec {

    @Nullable
    private TableMetaData table;
    @Nullable
    private List<SelectField> columns;
    @Nullable
    private List<JoinSpec> joins;
    @Nullable
    private List<ConditionSpec> whereConditions;
    @Nullable
    private List<OrderBySpec> orderBys;
    @Nullable
    private LimitSpec limit;


    public @Nullable TableMetaData getTable() {
        return table;
    }

    public void setTable(final TableMetaData table) {
        this.table = table;
    }

    public @Nullable List<SelectField> getColumns() {
        return columns;
    }

    public void setColumns(final List<SelectField> columns) {
        this.columns = columns;
    }

    public @Nullable List<JoinSpec> joins() {
        return joins;
    }

    public JoinSpec newJoinSpec(final String table) {
        if (this.joins == null) {
            joins = new ArrayList<>();
        }

        final JoinSpec joinSpec = new JoinSpec(table);
        joins.add(joinSpec);
        return joinSpec;
    }

    public @Nullable List<ConditionSpec> whereConditions() {
        return whereConditions;
    }

    public ConditionSpec newWhereCondition(final String column) {
        if (this.whereConditions == null) {
            whereConditions = new ArrayList<>();
        }

        final ConditionSpec conditionSpec = new ConditionSpec();
        conditionSpec.setColumn(column);
        whereConditions.add(conditionSpec);
        return conditionSpec;
    }

    public @Nullable List<OrderBySpec> orderBys() {
        return orderBys;
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
}
