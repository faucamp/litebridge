package org.litebridge.db.spi.impl.function.aggregate;

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
     * @param columnIdentifierGenerator The generator for column identifiers.
     */
    public Avg(final ColumnExpression target, ColumnIdentifierGenerator columnIdentifierGenerator) {
        super(target, columnIdentifierGenerator);
    }

    @Override
    protected String template() {
        return "AVG(%s)";
    }
}
