package org.litebridge.db.spi.expression;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Column;

/**
 * Factory to create expressions targeting a specific column.
 */
@FunctionalInterface
public interface ColumnExpressionFactory {

    /**
     * Creates a column expression.
     *
     * @param column Target column of the expression.
     * @param args   Expression-specific additional arguments, if any.
     * @return A new column expression.
     */
    ColumnExpression create(Column column, @Nullable Object... args);
}
