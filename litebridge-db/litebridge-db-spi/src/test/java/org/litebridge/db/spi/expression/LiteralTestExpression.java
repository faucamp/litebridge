package org.litebridge.db.spi.expression;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Operation;

public class LiteralTestExpression implements LiteralExpression {

    private final @Nullable Object value;
    private final @Nullable String alias;

    public LiteralTestExpression(@Nullable final Object value) {
        this.value = value;
        this.alias = null;
    }

    public LiteralTestExpression(@Nullable final Object value, @Nullable final String alias) {
        this.value = value;
        this.alias = alias;
    }

    @Override
    public @Nullable Object value() {
        return value;
    }

    @Override
    public String alias() {
        return alias;
    }

    @Override
    public String toSql(final Operation operation, final ClauseType clause, final @Nullable DelegateExpression parent) {
        return value != null ? "NULL" : value.toString();
    }
}
