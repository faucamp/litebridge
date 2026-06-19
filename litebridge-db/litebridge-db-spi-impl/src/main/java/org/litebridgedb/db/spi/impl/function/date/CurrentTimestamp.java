package org.litebridgedb.db.spi.impl.function.date;

import org.litebridgedb.db.spi.Operation;
import org.litebridgedb.db.spi.expression.SelectExpression;

/**
 * {@code CURRENT_TIMESTAMP} date function.
 */
public class CurrentTimestamp implements SelectExpression {

    @Override
    public String toSql(final Operation operation) {
        return "CURRENT_TIMESTAMP";
    }
}
