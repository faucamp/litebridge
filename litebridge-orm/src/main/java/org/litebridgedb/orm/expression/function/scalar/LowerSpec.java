package org.litebridgedb.orm.expression.function.scalar;

import org.litebridgedb.orm.expression.ColumnExpressionSpec;
import org.litebridgedb.orm.expression.StringTONestableExpressionSpec;

/**
 * {@code LOWER()}: Returns the lowercase rhs of a lhs.
 *
 * @param target The target lhs/nested expression.
 */
public record LowerSpec(ColumnExpressionSpec target) implements StringTONestableExpressionSpec {
}
