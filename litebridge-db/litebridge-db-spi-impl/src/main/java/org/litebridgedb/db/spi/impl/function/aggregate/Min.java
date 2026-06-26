package org.litebridgedb.db.spi.impl.function.aggregate;

import org.litebridgedb.db.spi.expression.ColumnExpression;
import org.litebridgedb.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridgedb.db.spi.impl.function.FunctionExpression;

/**
 * {@code MIN(lhs)} aggregate function.
 */
public class Min extends FunctionExpression {

    public Min(final ColumnExpression target, ColumnIdentifierGenerator columnIdentifierGenerator) {
        super(target, columnIdentifierGenerator);
    }

    @Override
    protected String template() {
        return "MIN(%s)";
    }
}
