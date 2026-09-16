package org.litebridge.orm.expression.function.scalar;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.expression.AbstractColumnExpressionSpec;
import org.litebridge.orm.expression.ColumnExpressionSpec;
import org.litebridge.orm.expression.StringTODelegateExpressionSpec;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * {@code SUBSTRING()}: Returns a substring of a column's text.
 *
 */
public final class SubstringSpec extends AbstractColumnExpressionSpec implements StringTODelegateExpressionSpec {

    private final ColumnExpressionSpec target;
    private final int start;
    private final @Nullable Integer length;

    /**
     * @param target Target column expression to extract characters from.
     * @param start  The starting position. The first character of a database string is always 1.
     * @param length The number of characters to return. If {@code null}, the function extracts everything from the start position to the end of the text.
     */
    public SubstringSpec(final ColumnExpressionSpec target, final int start, final @Nullable Integer length) {
        this.target = target;
        this.start = start;
        this.length = length;
    }

    @Override
    public ColumnExpressionSpec target() {
        return target;
    }

    @Override
    public void setTableAlias(final String tableAlias) {
        // Propagate the table alias to the lower level
        target.setTableAlias(tableAlias);
    }

    public int start() {
        return start;
    }

    public @Nullable Integer length() {
        return length;
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (SubstringSpec) obj;
        return this.start == that.start
                && Objects.equals(this.alias, that.alias)
                && Objects.equals(this.target, that.target)
                && Objects.equals(this.length, that.length);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alias, target, start, length);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SubstringSpec.class.getSimpleName() + "[", "]")
                .add("target=" + target)
                .add("start=" + start)
                .add("length=" + length)
                .add("alias='" + alias + "'")
                .toString();
    }
}
