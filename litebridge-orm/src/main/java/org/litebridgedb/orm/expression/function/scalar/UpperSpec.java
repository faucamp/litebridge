package org.litebridgedb.orm.expression.function.scalar;

import org.litebridgedb.orm.expression.ColumnExpression;
import org.litebridgedb.orm.expression.StringTONestableExpression;

/**
 * {@code UPPER()}: Returns the uppercase value of a column.
 *
 * @param target The target column/nested expression.
 */
public record UpperSpec(ColumnExpression target) implements StringTONestableExpression {
}
