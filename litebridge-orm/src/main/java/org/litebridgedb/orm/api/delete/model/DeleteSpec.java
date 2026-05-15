package org.litebridgedb.orm.api.delete.model;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.commons.ObjectUtils;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.update.Delete;
import org.litebridgedb.orm.api.select.model.ConditionSpec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base specification for constructing a SQL DELETE statement.
 */
public class DeleteSpec {

    @Nullable
    protected Table table;
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

    public Delete toDelete() {
        ObjectUtils.requireNonNull(table, () -> new IllegalStateException("Table not specified"));

        return new Delete(table,
                whereConditions != null ? whereConditions.stream()
                        .map(ConditionSpec::toCondition)
                        .toList() : Collections.emptyList());
    }
}
