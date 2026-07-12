package org.litebridge.db.spi.impl.function.date;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.expression.DelegateExpression;
import org.litebridge.db.spi.expression.SelectExpression;

/**
 * {@code CURRENT_TIMESTAMP} date function.
 */
public class CurrentTimestamp implements SelectExpression {

    @Override
    public String toSql(final Operation operation, final ClauseType context, final @Nullable DelegateExpression parent) {
        return "CURRENT_TIMESTAMP";
    }
}
