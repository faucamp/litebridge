package org.litebridge.orm.expression;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.expression.function.aggregate.AvgSpec;
import org.litebridge.orm.expression.function.aggregate.MaxSpec;
import org.litebridge.orm.expression.function.aggregate.MinSpec;

import java.util.Objects;
import java.util.StringJoiner;

public abstract sealed class AbstractTODelegateExpressionSpec<T>
        extends AbstractColumnExpressionSpec
        implements DelegateExpressionSpec, TypeOverrideExpressionSpec<T> permits NumberTODelegateExpressionSpec, AvgSpec, MaxSpec, MinSpec {

    protected final ColumnExpressionSpec target;
    protected final Class<T> returnType;

    /**
     * Creates a new {@code AbstractTODelegateExpressionSpec} instance.
     *
     * @param target     The target nested expression
     * @param returnType The return type of the expression result.
     */
    protected AbstractTODelegateExpressionSpec(final ColumnExpressionSpec target, final Class<T> returnType) {
        this.target = target;
        this.returnType = returnType;
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
    public Class<T> returnType() {
        return returnType;
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (AbstractTODelegateExpressionSpec<?>) obj;
        return Objects.equals(this.alias, that.alias)
                && Objects.equals(this.target, that.target)
                && Objects.equals(this.returnType, that.returnType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alias, target, returnType);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", getClass().getSimpleName() + "[", "]")
                .add("alias='" + alias + "'")
                .add("target=" + target)
                .add("returnType=" + returnType)
                .toString();
    }
}
