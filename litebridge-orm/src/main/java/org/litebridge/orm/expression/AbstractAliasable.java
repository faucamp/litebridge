package org.litebridge.orm.expression;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.expression.function.aggregate.CountSpec;
import org.litebridge.orm.expression.select.LiteralExpressionSpec;

import java.util.Objects;

public abstract sealed class AbstractAliasable implements Aliasable permits AbstractColumnExpressionSpec, ProtoNestableBasicExprSpec, ProtoNestableTOExpr, CountSpec, LiteralExpressionSpec {

    protected @Nullable String alias;

    @Override
    public @Nullable String getAlias() {
        return alias;
    }

    @Override
    public void setAlias(@Nullable final String alias) {
        this.alias = alias;
    }

    @Override
    public AbstractAliasable as(final String alias) {
        this.alias = alias;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof final AbstractAliasable that)) return false;
        return Objects.equals(alias, that.alias);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(alias);
    }
}
