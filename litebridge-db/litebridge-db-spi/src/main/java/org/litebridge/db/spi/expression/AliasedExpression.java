package org.litebridge.db.spi.expression;

import org.jspecify.annotations.Nullable;

public interface AliasedExpression extends SelectExpression {

    /**
     * Retrieves the alias of this expression.
     *
     * @return The optional alias.
     */
    String alias();

    /**
     * Retrieves the source/parent table alias of this expression.
     *
     * @return the source/parent table alias of this expression
     */
    default @Nullable String tableAlias() {
        return null;
    }
}
