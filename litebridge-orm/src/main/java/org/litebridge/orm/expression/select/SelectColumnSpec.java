package org.litebridge.orm.expression.select;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Column;
import org.litebridge.orm.expression.AbstractColumnExpressionSpec;

import java.util.Objects;

/**
 * Expression that selects a database column.
 */
public sealed class SelectColumnSpec extends AbstractColumnExpressionSpec implements SelectTargetSpec permits SelectFieldSpec {

    private Column column;
    protected @Nullable String alias;

    /**
     * Constructor.
     *
     * @param column The column to select.
     */
    public SelectColumnSpec(Column column) {
        this.column = column;
    }

    public SelectColumnSpec(final Column column, final @Nullable String alias, final @Nullable String tableAlias) {
        this.column = column;
        this.alias = alias;
        this.tableAlias = tableAlias;
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
    public @Nullable String getAlias() {
        return alias;
    }

    @Override
    public void setAlias(@Nullable final String alias) {
        this.alias = alias;
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
