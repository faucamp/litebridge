package org.litebridgedb.orm.expression.function.aggregate;

import org.litebridgedb.orm.expression.ColumnExpressionSpec;
import org.litebridgedb.orm.expression.NestableExpressionSpec;
import org.litebridgedb.orm.expression.TypeOverrideExpressionSpec;

/**
 * {@code MIN()}: Returns the lowest or smallest value within a specified column or expression
 *
 * @param target     The target nested expression
 * @param returnType The return type of the expression result.
 * @param <T>        The return type of the expression result.
 */
public record MinSpec<T>(ColumnExpressionSpec target, Class<T> returnType)
        implements NestableExpressionSpec, TypeOverrideExpressionSpec<T> {
}
