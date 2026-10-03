package org.litebridge.orm.expression;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.expression.AliasReference;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.expression.DelegateExpression;

public class TestAliasReference implements AliasReference {

    protected final @Nullable String alias;

    public TestAliasReference(@Nullable final String alias) {
        this.alias = alias;
    }

    @Override
    public @Nullable String alias() {
        return alias;
    }

    @Override
    public @Nullable String tableAlias() {
        return null;
    }

    @Override
    public String toSql(final Operation operation, final ClauseType clause, final @Nullable DelegateExpression parent) {
        return alias != null ? alias : "";
    }
}
