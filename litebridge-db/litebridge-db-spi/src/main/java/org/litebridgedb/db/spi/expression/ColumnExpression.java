package org.litebridgedb.db.spi.expression;

import org.litebridgedb.db.spi.Column;

public interface ColumnExpression extends SelectExpression {
    /**
     * Retrieves the target lhs of this expression.
     *
     * @return The target lhs.
     */
    Column column();
}
