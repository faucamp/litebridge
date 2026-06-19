package org.litebridgedb.orm.expression.function.java;

import org.litebridgedb.orm.expression.ColumnExpression;
import org.litebridgedb.orm.expression.NestableExpression;
import org.litebridgedb.orm.expression.TypeOverrideExpression;

/**
 * Converts a database result into the specified Java type.
 * <p>
 * This uses Litebridge's registered type converter to perform the conversion;
 * it is not a database operation.
 */
public record ConvertSpec<T>(ColumnExpression target, Class<T> returnType)
        implements TypeOverrideExpression<T> {
}
