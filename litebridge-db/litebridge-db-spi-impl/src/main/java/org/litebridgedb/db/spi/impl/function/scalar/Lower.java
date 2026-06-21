package org.litebridgedb.db.spi.impl.function.scalar;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.expression.ColumnExpression;
import org.litebridgedb.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridgedb.db.spi.impl.function.FunctionExpression;

/**
 * {@code LOWER(column)} scalar function.
 */
public class Lower extends FunctionExpression {

    public Lower(final ColumnExpression target, final ColumnIdentifierGenerator columnIdentifierGenerator) {
        super(target, columnIdentifierGenerator);
    }

    @Override
    protected String template() {
        return "LOWER(%s)";
    }
}
