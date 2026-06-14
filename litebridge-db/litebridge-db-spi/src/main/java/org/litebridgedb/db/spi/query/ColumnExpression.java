package org.litebridgedb.db.spi.query;

import org.litebridgedb.db.spi.Column;

public abstract class ColumnExpression implements SelectExpression {

    protected final Column column;

    public ColumnExpression(Column column) {
        this.column = column;
    }

    public Column column() {
        return column;
    }
}
