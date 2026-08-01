package org.litebridge.orm.expression.select;

import org.litebridge.db.spi.Column;
import org.litebridge.orm.expression.ColumnExpressionSpec;

import java.util.Objects;

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

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof final SelectColumnSpec that)) return false;
        return Objects.equals(column, that.column);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(column);
    }
}
