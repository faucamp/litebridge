package org.litebridgedb.orm.expression.function.scalar;

import org.litebridgedb.orm.expression.ColumnExpression;
import org.litebridgedb.orm.expression.NumberTONestableExpression;

/**
 * {@code ABS()}: Absolute value of a number.
 *
 * @param target The target column/nested expression.
 */
public record AbsSpec(ColumnExpression target) implements NumberTONestableExpression {
}
