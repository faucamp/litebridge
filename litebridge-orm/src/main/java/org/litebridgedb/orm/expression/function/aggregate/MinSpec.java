package org.litebridgedb.orm.expression.function.aggregate;

import org.litebridgedb.orm.expression.ColumnExpression;
import org.litebridgedb.orm.expression.NestableExpression;
import org.litebridgedb.orm.expression.TypeOverrideExpression;

/**
 * {@code MIN()}: Returns the lowest or smallest value within a specified column or expression
 */
public record MinSpec<T>(ColumnExpression target, Class<T> returnType)
        implements NestableExpression, TypeOverrideExpression<T> {
}
