package org.litebridge.orm.expression.function.aggregate;

import org.litebridge.orm.expression.ColumnExpressionSpec;
import org.litebridge.orm.expression.DelegateExpressionSpec;
import org.litebridge.orm.expression.TypeOverrideExpressionSpec;

/**
 * {@code MIN()}: Returns the lowest or smallest value within a specified column or expression
 *
 * @param target     The target nested expression
 * @param returnType The return type of the expression result.
 * @param <T>        The return type of the expression result.
 */
public record MinSpec<T>(ColumnExpressionSpec target, Class<T> returnType)
        implements DelegateExpressionSpec, TypeOverrideExpressionSpec<T> {
}
