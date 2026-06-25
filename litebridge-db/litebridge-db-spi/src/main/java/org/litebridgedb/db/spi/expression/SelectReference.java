package org.litebridgedb.db.spi.expression;

import org.litebridgedb.db.spi.Column;

/**
 * Reference to another column in the select query.
 * <p>
 * This is used to reference a selected column in condition clauses.
 */
public abstract class SelectReference extends ColumnExpressionImpl {

    /**
     * Constructor.
     *
     * @param column The target selected column to reference.
     */
    protected SelectReference(final Column column) {
        super(column);
    }
}
