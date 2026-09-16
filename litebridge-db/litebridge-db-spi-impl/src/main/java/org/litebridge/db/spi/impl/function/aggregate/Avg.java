package org.litebridge.db.spi.impl.function.aggregate;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.expression.ColumnExpression;
import org.litebridge.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridge.db.spi.impl.function.FunctionExpression;

/**
 * {@code AVG(column)} aggregate function.
 */
public class Avg extends FunctionExpression {

    /**
     * Constructs a new {@code AVG} function expression.
     *
     * @param target                    The column expression to apply the function to.
     * @param alias                     The alias for the column expression
     */
    public Avg(final ColumnExpression target, final @Nullable String alias) {
        super(target, alias);
    }

    @Override
    protected String template() {
        return "AVG(%s)";
    }
}
