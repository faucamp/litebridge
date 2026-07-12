package org.litebridge.orm.api.select.impl;

import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.query.Join;
import org.litebridge.orm.api.select.model.JoinSpec;
import org.litebridge.orm.api.select.model.SelectExpressionMapper;

import java.util.Collections;

public abstract class AbstractJoinSpec extends AbstractConditionBasedSpec implements JoinSpec {

    public AbstractJoinSpec(final Table table, final SelectExpressionMapper selectExpressionMapper) {
        super(table, selectExpressionMapper);
    }

    @Override
    public Join toJoin() {
        return new Join(table, conditions.toConditionGroup(selectExpressionMapper, Collections.singleton(table)));
    }
}
