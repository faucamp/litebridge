package org.litebridge.orm.expression;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.expression.function.aggregate.CountSpec;
import org.litebridge.orm.expression.select.LiteralExpressionSpec;

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
}
