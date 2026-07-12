package org.litebridge.db.spi.impl.function.aggregate;

import org.litebridge.db.spi.expression.ColumnExpression;
import org.litebridge.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridge.db.spi.impl.function.FunctionExpression;

/**
 * {@code MIN(column)} aggregate function.
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
