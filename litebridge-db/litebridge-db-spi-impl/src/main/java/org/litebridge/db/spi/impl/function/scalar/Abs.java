package org.litebridge.db.spi.impl.function.scalar;

import org.litebridge.db.spi.expression.ColumnExpression;
import org.litebridge.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridge.db.spi.impl.function.FunctionExpression;

/**
 * {@code ABS(column)} scalar function.
 */
public class Abs extends FunctionExpression {

    /**
     * Constructs a new {@code ABS} function expression.
     *
     * @param target                    The column expression to apply the function to.
     * @param columnIdentifierGenerator The generator for column identifiers.
     */
    public Abs(final ColumnExpression target, final ColumnIdentifierGenerator columnIdentifierGenerator) {
        super(target, columnIdentifierGenerator);
    }

    @Override
    protected String template() {
        return "ABS(%s)";
    }
}
