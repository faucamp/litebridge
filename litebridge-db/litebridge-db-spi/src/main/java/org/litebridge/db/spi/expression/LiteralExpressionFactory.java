package org.litebridge.db.spi.expression;

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

    /**
     * Creates a literal value expression that may be treated as a bind parameter.
     *
     * @param value     The literal value to be represented.
     * @param parameter Whether this literal should be treated as a bind parameter.
     * @return A new literal expression.
     */
    default LiteralExpression create(@Nullable Object value, boolean parameter) {
        return new LiteralExpression(value, parameter);
    }
}
