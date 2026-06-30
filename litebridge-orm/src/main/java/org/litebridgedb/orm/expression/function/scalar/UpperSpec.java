package org.litebridgedb.orm.expression.function.scalar;

import org.litebridgedb.orm.expression.ColumnExpressionSpec;
import org.litebridgedb.orm.expression.StringTODelegateExpressionSpec;

/**
 * {@code UPPER()}: Returns the uppercase value of a column.
 *
 * @param target The target column/nested expression.
 */
public record UpperSpec(ColumnExpressionSpec target) implements StringTODelegateExpressionSpec {
}
