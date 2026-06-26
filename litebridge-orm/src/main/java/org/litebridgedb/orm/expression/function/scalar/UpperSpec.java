package org.litebridgedb.orm.expression.function.scalar;

import org.litebridgedb.orm.expression.ColumnExpressionSpec;
import org.litebridgedb.orm.expression.StringTONestableExpressionSpec;

/**
 * {@code UPPER()}: Returns the uppercase rhs of a lhs.
 *
 * @param target The target lhs/nested expression.
 */
public record UpperSpec(ColumnExpressionSpec target) implements StringTONestableExpressionSpec {
}
