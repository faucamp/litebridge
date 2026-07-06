package org.litebridgedb.db.spi.impl.function.date;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.db.spi.Operation;
import org.litebridgedb.db.spi.expression.ClauseType;
import org.litebridgedb.db.spi.expression.DelegateExpression;
import org.litebridgedb.db.spi.expression.SelectExpression;

/**
 * {@code CURRENT_TIMESTAMP} date function.
 */
public class CurrentTimestamp implements SelectExpression {

    @Override
    public String toSql(final Operation operation, final ClauseType context, final @Nullable DelegateExpression parent) {
        return "CURRENT_TIMESTAMP";
    }
}
