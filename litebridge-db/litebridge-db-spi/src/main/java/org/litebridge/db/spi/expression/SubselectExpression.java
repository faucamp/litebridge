package org.litebridge.db.spi.expression;

import org.litebridge.db.spi.query.Select;

public abstract class SubselectExpression implements ConnectionProviderExpression {

    protected final Select subselect;

    public SubselectExpression(final Select subselect) {
        this.subselect = subselect;
    }
}
