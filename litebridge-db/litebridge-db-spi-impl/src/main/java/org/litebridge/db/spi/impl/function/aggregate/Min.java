package org.litebridge.db.spi.impl.function.aggregate;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.expression.ColumnExpression;
import org.litebridge.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridge.db.spi.impl.function.FunctionExpression;

/**
 * {@code MIN(column)} aggregate function.
 */
public class Min extends FunctionExpression {

    /**
     * Creates a new {@code MIN} aggregate function.
     *
     * @param target                    the target column expression
     * @param alias                     the alias for the column expression
     */
    public Min(final ColumnExpression target, final @Nullable String alias) {
        super(target, alias);
    }

    @Override
    protected String template() {
        return "MIN(%s)";
    }
}
