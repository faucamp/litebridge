package org.litebridge.orm.expression.function.scalar;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.expression.ColumnExpressionSpec;
import org.litebridge.orm.expression.StringTODelegateExpressionSpec;

/**
 * {@code SUBSTRING()}: Returns a substring of a column's text.
 *
 * @param target Target column expression to extract characters from.
 * @param start  The starting position. The first character of a database string is always 1.
 * @param length The number of characters to return. If {@code null}, the function extracts everything from the start position to the end of the text.
 */
public record SubstringSpec(ColumnExpressionSpec target, int start, @Nullable Integer length)
        implements StringTODelegateExpressionSpec {
}
