package org.litebridge.db.spi.impl.function.aggregate;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.expression.DelegateExpression;
import org.litebridge.db.spi.expression.SelectExpression;

/**
 * {@code COUNT(*)} aggregate function.
 */
public class Count implements SelectExpression {

    @Override
    public String toSql(final Operation operation, final ClauseType context, final @Nullable DelegateExpression parent) {
        return "COUNT(*)";
    }
}
