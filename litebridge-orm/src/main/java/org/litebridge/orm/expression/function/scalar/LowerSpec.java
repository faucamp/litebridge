package org.litebridge.orm.expression.function.scalar;

import org.litebridge.orm.expression.ColumnExpressionSpec;
import org.litebridge.orm.expression.StringTODelegateExpressionSpec;

/**
 * {@code LOWER()}: Returns the lowercase value of a column.
 *
 * @param target The target column/nested expression.
 */
public record LowerSpec(ColumnExpressionSpec target) implements StringTODelegateExpressionSpec {
}
