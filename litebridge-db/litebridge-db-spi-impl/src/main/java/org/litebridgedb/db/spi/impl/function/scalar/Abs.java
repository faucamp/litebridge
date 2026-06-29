package org.litebridgedb.db.spi.impl.function.scalar;

import org.litebridgedb.db.spi.expression.ColumnExpression;
import org.litebridgedb.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridgedb.db.spi.impl.function.FunctionExpression;

/**
 * {@code ABS(column)} scalar function.
 */
public class Abs extends FunctionExpression {

    public Abs(final ColumnExpression target, final ColumnIdentifierGenerator columnIdentifierGenerator) {
        super(target, columnIdentifierGenerator);
    }

    @Override
    protected String template() {
        return "ABS(%s)";
    }
}
