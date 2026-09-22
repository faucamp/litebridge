package org.litebridge.db.spi.expression;

import org.jspecify.annotations.Nullable;

public interface LiteralExpression extends AliasedExpression {

    /**
     * Retrieves the value of this literal expression.
     *
     * @return the literal value encapsulated by this expression
     */
    @Nullable Object value();
}
