package org.litebridge.db.spi.impl.function.scalar;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.expression.ColumnExpression;
import org.litebridge.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridge.db.spi.impl.function.FunctionExpression;

/**
 * {@code LOWER(column)} scalar function.
 */
public class Lower extends FunctionExpression {

    /**
     * Creates a new {@code LOWER} scalar function expression.
     *
     * @param target                    the target column expression
     * @param columnIdentifierGenerator the column identifier generator
     */
    public Lower(final ColumnExpression target, final ColumnIdentifierGenerator columnIdentifierGenerator) {
        super(target, columnIdentifierGenerator);
    }

    @Override
    protected String template() {
        return "LOWER(%s)";
    }
}
