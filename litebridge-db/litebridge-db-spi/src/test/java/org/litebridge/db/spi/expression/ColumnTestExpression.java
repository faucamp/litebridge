package org.litebridge.db.spi.expression;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Operation;

@NullMarked
public record ColumnTestExpression(Column column, @Nullable String alias,
                                   @Nullable String tableAlias) implements ColumnExpression {

    public ColumnTestExpression(final Column column) {
        this(column, null, null);
    }

    @Override
    public String toSql(final Operation operation, final ClauseType clause, final @Nullable DelegateExpression parent) {
        return column.name();
    }
}
