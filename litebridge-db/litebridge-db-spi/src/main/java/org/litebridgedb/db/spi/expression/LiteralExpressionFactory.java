package org.litebridgedb.db.spi.expression;

import org.jspecify.annotations.Nullable;

/**
 * Factory to create literal value expressions.
 */
@FunctionalInterface
public interface LiteralExpressionFactory {

    /**
     * Creates a literal value expression.
     *
     * @param value The literal value to be represented.
     * @return A new literal expression.
     */
    LiteralExpression create(@Nullable Object value);
}
