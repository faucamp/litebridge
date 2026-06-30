package org.litebridgedb.orm.expression;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.orm.expression.select.SelectColumnSpec;

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
