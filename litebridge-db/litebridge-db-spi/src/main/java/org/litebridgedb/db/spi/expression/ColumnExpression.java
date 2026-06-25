package org.litebridgedb.db.spi.expression;

import org.litebridgedb.db.spi.Column;

public interface ColumnExpression extends SelectExpression {
    /**
     * Retrieves the target column of this expression.
     *
     * @return The target column.
     */
    Column column();
}
