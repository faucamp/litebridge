package org.litebridge.orm.expression.function.scalar;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.expression.AbstractColumnExpressionSpec;
import org.litebridge.orm.expression.ColumnExpressionSpec;
import org.litebridge.orm.expression.StringTODelegateExpressionSpec;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * {@code LOWER()}: Returns the lowercase value of a column.
 *
 */
public final class LowerSpec extends AbstractColumnExpressionSpec implements StringTODelegateExpressionSpec {

    private final ColumnExpressionSpec target;

    /**
     * @param target The target column/nested expression.
     */
    public LowerSpec(final ColumnExpressionSpec target, final @Nullable String alias) {
        this.target = target;
        this.alias = alias;
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

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (LowerSpec) obj;
        return Objects.equals(this.alias, that.alias) && Objects.equals(this.target, that.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alias, target);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", LowerSpec.class.getSimpleName() + "[", "]")
                .add("target=" + target)
                .add("alias='" + alias + "'")
                .toString();
    }
}
