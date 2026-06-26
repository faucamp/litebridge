package org.litebridgedb.db.spi.expression;

import org.jspecify.annotations.Nullable;

/**
 * Factory to create nestable lhs expressions.
 */
@FunctionalInterface
public interface DelegateExpressionFactory {

    /**
     * Creates a nestable lhs expression.
     *
     * @param target Target/nested lhs expression, e.g. target "select lhs" expression
     * @param args   Expression-specific additional arguments, if any.
     * @return A new lhs expression.
     */
    DelegateColumnExpression create(ColumnExpression target, @Nullable Object... args);
}
