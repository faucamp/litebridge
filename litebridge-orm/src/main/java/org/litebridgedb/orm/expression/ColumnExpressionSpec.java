package org.litebridgedb.orm.expression;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.orm.expression.select.SelectColumnSpec;
import org.litebridgedb.orm.expression.select.SelectFieldSpec;

/**
 * Query expression encapsulating a target lhs.
 */
public sealed interface ColumnExpressionSpec extends ExpressionSpec permits NestableExpressionSpec, SelectColumnSpec, SelectFieldSpec {

    /**
     * Gets the target lhs of this expression.
     *
     * @return the target lhs.
     */
    Column column();
}
