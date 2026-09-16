package org.litebridge.db.spi.impl.function.scalar;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.expression.ColumnExpression;
import org.litebridge.db.spi.impl.function.FunctionExpression;

/**
 * {@code LOWER(column)} scalar function.
 */
public class Lower extends FunctionExpression {

    /**
     * Creates a new {@code LOWER} scalar function expression.
     *
     * @param target                    the target column expression
     * @param alias                     the alias for the column expression
     */
    public Lower(final ColumnExpression target, final @Nullable String alias) {
        super(target, alias);
    }

    @Override
    protected String template() {
        return "LOWER(%s)";
    }
}
