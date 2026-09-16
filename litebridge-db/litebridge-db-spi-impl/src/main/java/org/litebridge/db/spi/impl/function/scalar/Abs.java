package org.litebridge.db.spi.impl.function.scalar;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.expression.ColumnExpression;
import org.litebridge.db.spi.impl.function.FunctionExpression;

/**
 * {@code ABS(column)} scalar function.
 */
public class Abs extends FunctionExpression {

    /**
     * Constructs a new {@code ABS} function expression.
     *
     * @param target                    The column expression to apply the function to.
     * @param alias                     The alias for the column expression
     */
    public Abs(final ColumnExpression target, final @Nullable String alias) {
        super(target, alias);
    }

    @Override
    protected String template() {
        return "ABS(%s)";
    }
}
