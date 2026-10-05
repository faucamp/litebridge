package org.litebridge.db.spi.expression;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.ColumnType;

/**
 * Factory to create bind value expressions.
 */
@FunctionalInterface
public interface BindValueExpressionFactory {

    /**
     * Creates a bind value expression.
     *
     * @param index      The index of the bind value.
     * @param size       The size of the bind value.
     * @param columnType The type of the bind value.
     * @param alias      The alias of the bind value.
     * @return A new bind value expression.
     */
    BindValueExpression create(int index, int size, ColumnType columnType, @Nullable String alias);
}
