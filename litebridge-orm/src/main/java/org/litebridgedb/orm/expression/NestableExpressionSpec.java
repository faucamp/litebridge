package org.litebridgedb.orm.expression;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.orm.expression.function.aggregate.AvgSpec;
import org.litebridgedb.orm.expression.function.aggregate.MaxSpec;
import org.litebridgedb.orm.expression.function.aggregate.MinSpec;

/**
 * A query expression that can nest another query expression.
 */
public sealed interface NestableExpressionSpec extends ColumnExpressionSpec
        permits NumberTONestableExpressionSpec, StringTONestableExpressionSpec, AvgSpec, MaxSpec, MinSpec {

    /**
     * Gets the target nested expression.
     *
     * @return the target nested expression.
     */
    ColumnExpressionSpec target();

    /**
     * Gets the target column of this expression.
     * <p>
     * The default implementation delegates to {@link #target()}.
     *
     * @return the target column.
     */
    default Column column() {
        return target().column();
    }
}
