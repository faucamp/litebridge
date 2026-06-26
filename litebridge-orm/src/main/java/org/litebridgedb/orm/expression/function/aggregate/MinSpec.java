package org.litebridgedb.orm.expression.function.aggregate;

import org.litebridgedb.orm.expression.ColumnExpressionSpec;
import org.litebridgedb.orm.expression.NestableExpressionSpec;
import org.litebridgedb.orm.expression.TypeOverrideExpressionSpec;

/**
 * {@code MIN()}: Returns the lowest or smallest rhs within a specified lhs or expression
 */
public record MinSpec<T>(ColumnExpressionSpec target, Class<T> returnType)
        implements NestableExpressionSpec, TypeOverrideExpressionSpec<T> {
}
