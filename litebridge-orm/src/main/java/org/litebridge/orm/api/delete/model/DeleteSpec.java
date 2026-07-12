package org.litebridge.orm.api.delete.model;

import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.update.Delete;
import org.litebridge.orm.api.select.impl.AbstractConditionBasedSpec;
import org.litebridge.orm.api.select.model.SelectExpressionMapper;

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
