package org.litebridge.orm.expression;

import org.litebridge.db.spi.Column;
import org.litebridge.orm.expression.select.SelectColumnSpec;

/**
 * Query expression encapsulating a target column.
 */
public sealed interface ColumnExpressionSpec extends ExpressionSpec permits DelegateExpressionSpec, SelectColumnSpec {

    /**
     * Gets the target column of this expression.
     *
     * @return the target column.
     */
    Column getColumn();

    void setColumn(Column column);
}
