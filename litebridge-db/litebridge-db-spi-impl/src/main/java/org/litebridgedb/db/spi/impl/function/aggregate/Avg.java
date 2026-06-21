package org.litebridgedb.db.spi.impl.function.aggregate;

import org.litebridgedb.db.spi.expression.ColumnExpression;
import org.litebridgedb.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridgedb.db.spi.impl.function.FunctionExpression;

/**
 * {@code AVG(column)} aggregate function.
 */
public class Avg extends FunctionExpression {

    public Avg(final ColumnExpression target, ColumnIdentifierGenerator columnIdentifierGenerator) {
        super(target, columnIdentifierGenerator);
    }

    @Override
    protected String template() {
        return "AVG(%s)";
    }
}
