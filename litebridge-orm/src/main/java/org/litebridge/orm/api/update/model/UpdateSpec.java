package org.litebridge.orm.api.update.model;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.ObjectUtils;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.update.ColumnValue;
import org.litebridge.db.spi.update.Update;
import org.litebridge.orm.api.select.model.ConditionSpec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base specification for constructing a SQL DELETE statement.
 */
public class UpdateSpec {

    @Nullable
    protected Table table;
    protected final List<ColumnValue> columnValues = new ArrayList<>();
    @Nullable
    protected List<ConditionSpec> whereConditions;

    public Table getTable() {
        return ObjectUtils.requireNonNull(table, () -> new IllegalStateException("DeleteSpec.table not set"));
    }

    public void setTable(final Table table) {
        this.table = table;
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

    public void addColumnValue(final ColumnValue columnValue) {
        columnValues.add(columnValue);
    }

    public Update toUpdate() {
        ObjectUtils.requireNonNull(table, () -> new IllegalStateException("Table not specified"));

        return new Update(table,
                columnValues,
                whereConditions != null ? whereConditions.stream()
                        .map(ConditionSpec::toCondition)
                        .toList() : Collections.emptyList());
    }
}
