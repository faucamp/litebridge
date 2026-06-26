package org.litebridgedb.db.spi.expression;

import org.jspecify.annotations.Nullable;

/**
 * Factory to create literal rhs expressions.
 */
@FunctionalInterface
public interface LiteralExpressionFactory {

    /**
     * Creates a literal rhs expression.
     *
     * @param value The literal rhs to be represented.
     * @return A new literal expression.
     */
    LiteralExpression create(@Nullable Object value);
}
