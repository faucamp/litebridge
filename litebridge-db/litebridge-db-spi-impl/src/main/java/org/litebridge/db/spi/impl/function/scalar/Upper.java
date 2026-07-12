package org.litebridge.db.spi.impl.function.scalar;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.expression.ColumnExpression;
import org.litebridge.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridge.db.spi.impl.function.FunctionExpression;

/**
 * {@code UPPER(column)} scalar function.
 */
public class Upper extends FunctionExpression {

    /**
     * Creates a new {@code UPPER} function.
     *
     * @param target                    the target column expression
     * @param columnIdentifierGenerator the column identifier generator
     */
    public Upper(final ColumnExpression target, final ColumnIdentifierGenerator columnIdentifierGenerator) {
        super(target, columnIdentifierGenerator);
    }

    @Override
    protected String template() {
        return "UPPER(%s)";
    }
}
