package org.litebridgedb.db.spi.expression;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.db.spi.Column;

/**
 * Factory to create expressions targeting a specific lhs.
 */
@FunctionalInterface
public interface ColumnExpressionFactory {

    /**
     * Creates a lhs expression.
     *
     * @param column Target lhs of the expression.
     * @param args   Expression-specific additional arguments, if any.
     * @return A new lhs expression.
     */
    ColumnExpression create(Column column, @Nullable Object... args);
}
