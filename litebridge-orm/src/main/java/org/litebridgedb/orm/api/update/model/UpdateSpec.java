package org.litebridgedb.orm.api.update.model;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.commons.ObjectUtils;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.query.LogicOperator;
import org.litebridgedb.db.spi.update.ColumnValue;
import org.litebridgedb.db.spi.update.Update;
import org.litebridgedb.orm.api.select.model.ConditionGroupSpec;
import org.litebridgedb.orm.api.select.model.SelectExpressionMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base specification for constructing a SQL UPDATE statement.
 */
public class UpdateSpec {

    @Nullable
    protected Table table;
    protected final List<ColumnValue> columnValues = new ArrayList<>();
    @Nullable
    protected List<ConditionGroupSpec> whereConditions;

    private final SelectExpressionMapper selectExpressionMapper;

    public UpdateSpec(final SelectExpressionMapper selectExpressionMapper) {
        this.selectExpressionMapper = selectExpressionMapper;
    }

    public Table getTable() {
        return ObjectUtils.requireNonNull(table, () -> new IllegalStateException("UpdateSpec.table not set"));
    }

    public void setTable(final Table table) {
        this.table = table;
    }

    public ConditionGroupSpec newWhereConditionGroup(final LogicOperator logicOperator) {
        final ConditionGroupSpec conditionGroupSpec = new ConditionGroupSpec(logicOperator);
        whereConditions.add(conditionGroupSpec);
        return conditionGroupSpec;
    }

    public void addColumnValue(final ColumnValue columnValue) {
        columnValues.add(columnValue);
    }

    public Update toUpdate() {
        ObjectUtils.requireNonNull(table, () -> new IllegalStateException("Table not specified"));

        return new Update(table,
                columnValues,
                whereConditions != null ? whereConditions.stream()
                        .map(conditionGroupSpec -> conditionGroupSpec.toConditionGroup(selectExpressionMapper, Collections.singleton(table)))
                        .toList() : Collections.emptyList());
    }
}
