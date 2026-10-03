package org.litebridge.db.spi.expression;

import org.jspecify.annotations.Nullable;

public interface AliasReference extends AliasedExpression {

    /**
     * Retrieves the parent table alias for the expression.
     *
     * @return The optional parent table alias.
     */
    @Nullable String tableAlias();
}
