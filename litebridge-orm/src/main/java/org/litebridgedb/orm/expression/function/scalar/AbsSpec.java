package org.litebridgedb.orm.expression.function.scalar;

import org.litebridgedb.orm.expression.ColumnExpressionSpec;
import org.litebridgedb.orm.expression.NumberTONestableExpressionSpec;

/**
 * {@code ABS()}: Absolute value of a number.
 *
 * @param target The target column/nested expression.
 */
public record AbsSpec(ColumnExpressionSpec target) implements NumberTONestableExpressionSpec {
}
