package org.litebridgedb.orm.expression.select;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.orm.expression.ColumnExpressionSpec;

/**
 * Expression that selects a database column.
 */
public sealed class SelectColumnSpec implements ColumnExpressionSpec permits SelectFieldSpec {

    private Column column;

    /**
     * Constructor.
     *
     * @param column The column to select.
     */
    public SelectColumnSpec(Column column) {
        this.column = column;
    }

    @Override
    public Column getColumn() {
        return column;
    }

    @Override
    public void setColumn(final Column column) {
        this.column = column;
    }
}
