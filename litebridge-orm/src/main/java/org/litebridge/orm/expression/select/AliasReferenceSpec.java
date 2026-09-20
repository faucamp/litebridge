package org.litebridge.orm.expression.select;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.expression.ExpressionSpec;

public record AliasReferenceSpec(@Nullable String alias,
                                 @Nullable ExpressionSpec expression,
                                 @Nullable String tableAlias)
        implements ExpressionSpec, SelectTargetSpec {

    public AliasReferenceSpec(final String alias, final String tableAlias) {
        this(alias, null, tableAlias);
    }

    public AliasReferenceSpec(final ExpressionSpec expression, final String tableAlias) {
        this(null, expression, tableAlias);
    }

    public AliasReferenceSpec(final String alias) {
        this(alias, null, null);
    }
}
