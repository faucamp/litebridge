package org.litebridgedb.db.spi.expression;

import org.litebridgedb.db.spi.query.Select;

public abstract class SubselectExpression implements ConnectionProviderExpression {

    protected final Select subselect;

    public SubselectExpression(final Select subselect) {
        this.subselect = subselect;
    }
}
