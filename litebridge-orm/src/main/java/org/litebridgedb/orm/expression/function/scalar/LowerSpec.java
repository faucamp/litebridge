package org.litebridgedb.orm.expression.function.scalar;

import org.litebridgedb.orm.expression.ColumnExpression;
import org.litebridgedb.orm.expression.StringTONestableExpression;

/**
 * {@code LOWER()}: Returns the lowercase value of a column.
 *
 * @param target The target column/nested expression.
 */
public record LowerSpec(ColumnExpression target) implements StringTONestableExpression {
}
