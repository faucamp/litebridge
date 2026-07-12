package org.litebridge.orm.expression.function.scalar;

import org.litebridge.orm.expression.ColumnExpressionSpec;
import org.litebridge.orm.expression.NumberTODelegateExpressionSpec;

/**
 * {@code ABS()}: Absolute value of a number.
 *
 * @param target The target column/nested expression.
 */
public record AbsSpec(ColumnExpressionSpec target) implements NumberTODelegateExpressionSpec {
}
