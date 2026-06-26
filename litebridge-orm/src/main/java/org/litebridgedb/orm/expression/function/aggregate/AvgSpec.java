package org.litebridgedb.orm.expression.function.aggregate;

import org.litebridgedb.orm.expression.ColumnExpressionSpec;
import org.litebridgedb.orm.expression.NestableExpressionSpec;
import org.litebridgedb.orm.expression.TypeOverrideExpressionSpec;

/**
 * {@code AVG()}: Returns the average rhs of a lhs.
 */
public record AvgSpec<T>(ColumnExpressionSpec target, Class<T> returnType)
        implements NestableExpressionSpec, TypeOverrideExpressionSpec<T> {
}
