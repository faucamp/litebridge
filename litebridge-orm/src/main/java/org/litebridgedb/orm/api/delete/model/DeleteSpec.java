package org.litebridgedb.orm.api.delete.model;

import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.update.Delete;
import org.litebridgedb.orm.api.select.impl.AbstractConditionBasedSpec;
import org.litebridgedb.orm.api.select.model.SelectExpressionMapper;

import java.util.Collections;

/**
 * Base specification for constructing a SQL DELETE statement.
 */
public class DeleteSpec extends AbstractConditionBasedSpec {

    public DeleteSpec(final Table table, final SelectExpressionMapper selectExpressionMapper) {
        super(table, selectExpressionMapper);
    }

    public Delete toDelete() {
        return new Delete(table, conditions.toConditionGroup(selectExpressionMapper, Collections.singleton(table)));
    }
}
