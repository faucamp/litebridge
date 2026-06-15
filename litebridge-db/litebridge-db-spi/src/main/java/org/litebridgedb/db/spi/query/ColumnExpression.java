package org.litebridgedb.db.spi.query;

import org.litebridgedb.db.spi.Column;

public abstract class ColumnExpression implements SelectExpression {

    protected final Column column;

    protected ColumnExpression(final Column column) {
        this.column = column;
    }

    public final Column column() {
        return column;
    }
}
