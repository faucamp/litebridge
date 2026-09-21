package org.litebridge.orm.expression.select;

import org.litebridge.orm.expression.AbstractAliasable;
import org.litebridge.orm.expression.TypeOverrideExpressionSpec;

import java.util.Objects;

public final class LiteralExpressionSpec<T> extends AbstractAliasable implements TypeOverrideExpressionSpec<T> {

    private final T value;
    private final Class<T> returnType;

    @SuppressWarnings("unchecked")
    public LiteralExpressionSpec(final T value) {
        this.value = value;
        this.returnType = (Class<T>) value.getClass();
    }

    public T value() {
        return value;
    }

    @Override
    public Class<T> returnType() {
        return returnType;
    }

    @Override
    @SuppressWarnings("unchecked")
    public LiteralExpressionSpec<T> as(final String alias) {
        return (LiteralExpressionSpec<T>) super.as(alias);
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof final LiteralExpressionSpec<?> that)) return false;
        return Objects.equals(value, that.value)
                && Objects.equals(alias, that.alias)
                && Objects.equals(returnType, that.returnType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, alias, returnType);
    }
}
