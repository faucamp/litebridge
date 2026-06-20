package org.litebridgedb.orm.expression.function.aggregate;

import org.litebridgedb.orm.expression.ColumnExpressionSpec;
import org.litebridgedb.orm.expression.NestableExpressionSpec;
import org.litebridgedb.orm.expression.TypeOverrideExpressionSpec;

/**
 * {@code MAX()}: Returns the highest or largest value within a specified column or expression
 */
public record MaxSpec<T>(ColumnExpressionSpec target, Class<T> returnType)
        implements NestableExpressionSpec, TypeOverrideExpressionSpec<T> {
}
