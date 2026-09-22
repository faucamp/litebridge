package org.litebridge.db.spi.expression;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Operation;

public class ColumnTestExpression implements ColumnExpression {

    private final Column column;
    private final @Nullable String alias;
    private final @Nullable String tableAlias;

    public ColumnTestExpression(final Column column, @Nullable final String alias, @Nullable final String tableAlias) {
        this.column = column;
        this.alias = alias;
        this.tableAlias = tableAlias;
    }

    public ColumnTestExpression(final Column column) {
        this.column = column;
        this.alias = null;
        this.tableAlias = null;
    }

    @Override
    public Column column() {
        return column;
    }

    @Override
    public @Nullable String alias() {
        return alias;
    }

    @Override
    public @Nullable String tableAlias() {
        return tableAlias;
    }

    @Override
    public String toSql(final Operation operation, final ClauseType clause, final @Nullable DelegateExpression parent) {
        return column.name();
    }
}
