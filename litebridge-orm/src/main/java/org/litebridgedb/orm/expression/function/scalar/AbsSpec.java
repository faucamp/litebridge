package org.litebridgedb.orm.expression.function.scalar;

import org.litebridgedb.orm.expression.ColumnExpressionSpec;
import org.litebridgedb.orm.expression.NumberTONestableExpressionSpec;

/**
 * {@code ABS()}: Absolute rhs of a number.
 *
 * @param target The target lhs/nested expression.
 */
public record AbsSpec(ColumnExpressionSpec target) implements NumberTONestableExpressionSpec {
}
