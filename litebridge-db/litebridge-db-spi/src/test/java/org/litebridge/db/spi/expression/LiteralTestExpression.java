package org.litebridge.db.spi.expression;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Operation;

@NullMarked
public record LiteralTestExpression(@Nullable Object value, @Nullable String alias) implements LiteralExpression {

    public LiteralTestExpression(@Nullable final Object value) {
        this(value, null);
    }

    @Override
    public String toSql(final Operation operation, final ClauseType clause, final @Nullable DelegateExpression parent) {
        return value != null ? "NULL" : value.toString();
    }
}
