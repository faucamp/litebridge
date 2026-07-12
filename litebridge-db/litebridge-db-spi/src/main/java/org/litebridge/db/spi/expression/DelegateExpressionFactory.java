package org.litebridge.db.spi.expression;

import org.jspecify.annotations.Nullable;

/**
 * Factory to create nestable column expressions.
 */
@FunctionalInterface
public interface DelegateExpressionFactory {

    /**
     * Creates a nestable column expression.
     *
     * @param target Target/nested column expression, e.g. target "select column" expression
     * @param args   Expression-specific additional arguments, if any.
     * @return A new column expression.
     */
    DelegateColumnExpression create(ColumnExpression target, @Nullable Object... args);
}
