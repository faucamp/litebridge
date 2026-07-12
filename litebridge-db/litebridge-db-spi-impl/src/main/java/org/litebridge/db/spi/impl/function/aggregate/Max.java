package org.litebridge.db.spi.impl.function.aggregate;

import org.litebridge.db.spi.expression.ColumnExpression;
import org.litebridge.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridge.db.spi.impl.function.FunctionExpression;

/**
 * {@code MAX(column)} aggregate function.
 */
public class Max extends FunctionExpression {

    public Max(final ColumnExpression target, ColumnIdentifierGenerator columnIdentifierGenerator) {
        super(target, columnIdentifierGenerator);
    }

    @Override
    protected String template() {
        return "MAX(%s)";
    }
}
