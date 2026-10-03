package org.litebridge.db.spi.expression;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Operation;

public record ColumnTestExpression(Column column, @Nullable String alias,
                                   @Nullable String tableAlias) implements ColumnExpression {

    public ColumnTestExpression(final Column column) {
        this(column, null, null);
    }

    @Override
    public String toSql(final @NonNull Operation operation, final @NonNull ClauseType clause, final @Nullable DelegateExpression parent) {
        return column.name();
    }
}
