package org.litebridge.db.spi.expression;

import org.litebridge.db.spi.query.Select;

/**
 * A query expression that represents a subselect.
 */
public abstract class SubselectExpression implements ConnectionProviderExpression {

    /**
     * The subselect query.
     */
    protected final Select subselect;

    /**
     * Constructs a new {@code SubselectExpression} with the given subselect query.
     *
     * @param subselect the subselect query to be wrapped
     */
    public SubselectExpression(final Select subselect) {
        this.subselect = subselect;
    }
}
