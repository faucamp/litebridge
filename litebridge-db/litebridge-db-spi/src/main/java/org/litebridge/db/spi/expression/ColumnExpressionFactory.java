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
     * @param column     Target column of the expression.
     * @param alias      Optional column alias
     * @param tableAlias Optional table alias
     * @return A new column expression.
     */
    ColumnExpression create(Column column, @Nullable String alias, @Nullable String tableAlias);
}
