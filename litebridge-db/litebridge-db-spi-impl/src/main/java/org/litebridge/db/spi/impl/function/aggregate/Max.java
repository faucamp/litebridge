package org.litebridge.db.spi.impl.function.aggregate;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.expression.ColumnExpression;
import org.litebridge.db.spi.impl.function.FunctionExpression;

/**
 * {@code MAX(column)} aggregate function.
 */
public class Max extends FunctionExpression {

    /**
     * Creates a new {@code Max} function.
     *
     * @param target                    the target column expression
     * @param alias                     the alias for the column expression
     */
    public Max(final ColumnExpression target, final @Nullable String alias) {
        super(target, alias);
    }

    @Override
    protected String template() {
        return "MAX(%s)";
    }
}
