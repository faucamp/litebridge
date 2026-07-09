package org.litebridgedb.orm.api.update.model;

import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.update.ColumnValue;
import org.litebridgedb.db.spi.update.Update;
import org.litebridgedb.orm.api.select.impl.AbstractConditionBasedSpec;
import org.litebridgedb.orm.api.select.model.SelectExpressionMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base specification for constructing a SQL UPDATE statement.
 */
public class UpdateSpec extends AbstractConditionBasedSpec {

    protected final List<ColumnValue> columnValues = new ArrayList<>();

    public UpdateSpec(final Table table, final SelectExpressionMapper selectExpressionMapper) {
        super(table, selectExpressionMapper);
    }

    public void addColumnValue(final ColumnValue columnValue) {
        columnValues.add(columnValue);
    }

    public Update toUpdate() {
        return new Update(table,
                columnValues,
                conditions.toConditionGroup(selectExpressionMapper, Collections.singleton(table)));
    }
}
