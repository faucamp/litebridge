package org.litebridgedb.orm.expression.function.aggregate;

import org.litebridgedb.orm.expression.ColumnExpression;
import org.litebridgedb.orm.expression.NestableExpression;
import org.litebridgedb.orm.expression.TypeOverrideExpression;

/**
 * {@code AVG()}: Returns the average value of a column.
 */
public record AvgSpec<T>(ColumnExpression target, Class<T> returnType)
        implements NestableExpression, TypeOverrideExpression<T> {
}
