package org.litebridge.db.spi.impl.function.scalar;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.expression.ColumnExpression;
import org.litebridge.db.spi.impl.function.FunctionExpression;

/**
 * {@code UPPER(column)} scalar function.
 */
public class Upper extends FunctionExpression {

    /**
     * Creates a new {@code UPPER} function.
     *
     * @param target                    the target column expression
     * @param alias                     the alias for the column expression
     */
    public Upper(final ColumnExpression target, final @Nullable String alias) {
        super(target, alias);
    }

    @Override
    protected String template() {
        return "UPPER(%s)";
    }
}
