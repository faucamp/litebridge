package org.litebridge.orm.expression.select;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.expression.ExpressionSpec;

public record AliasReferenceSpec(String fromAlias,
                                 @Nullable String column,
                                 @Nullable ExpressionSpec expression)
        implements ExpressionSpec, SelectTargetSpec {

    public AliasReferenceSpec(final String fromAlias, final String column) {
        this(fromAlias, column, null);
    }

    public AliasReferenceSpec(final String fromAlias, final ExpressionSpec expression) {
        this(fromAlias, null, expression);
    }

    public AliasReferenceSpec(final String alias) {
        this(alias, null, null);
    }
}
