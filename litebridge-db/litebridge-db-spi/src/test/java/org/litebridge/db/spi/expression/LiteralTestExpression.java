package org.litebridge.db.spi.expression;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Operation;

public record LiteralTestExpression(@Nullable Object value, @Nullable String alias) implements LiteralExpression {

    public LiteralTestExpression(@Nullable final Object value) {
        this(value, null);
    }

    @Override
    public String toSql(final @NonNull Operation operation, final @NonNull ClauseType clause, final @Nullable DelegateExpression parent) {
        return value != null ? "NULL" : value.toString();
    }
}
