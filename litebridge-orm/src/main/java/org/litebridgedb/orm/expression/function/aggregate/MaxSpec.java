package org.litebridgedb.orm.expression.function.aggregate;

import org.litebridgedb.orm.expression.ColumnExpression;
import org.litebridgedb.orm.expression.NestableExpression;
import org.litebridgedb.orm.expression.TypeOverrideExpression;

/**
 * {@code MAX()}: Returns the highest or largest value within a specified column or expression
 */
public record MaxSpec<T>(ColumnExpression target, Class<T> returnType)
        implements NestableExpression, TypeOverrideExpression<T> {
}
