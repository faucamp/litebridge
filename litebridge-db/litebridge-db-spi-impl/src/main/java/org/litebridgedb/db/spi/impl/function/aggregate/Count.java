package org.litebridgedb.db.spi.impl.function.aggregate;

import org.litebridgedb.db.spi.Operation;
import org.litebridgedb.db.spi.expression.SelectExpression;

/**
 * {@code COUNT(*)} aggregate function.
 */
public class Count implements SelectExpression {

    @Override
    public String toSql(final Operation operation) {
        return "COUNT(*)";
    }
}
