package org.litebridge.orm.expression.select;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.expression.AbstractAliasable;
import org.litebridge.orm.expression.TypeOverrideExpressionSpec;

import java.util.Objects;

public final class LiteralExpressionSpec<T> extends AbstractAliasable implements TypeOverrideExpressionSpec<T> {

    private final @Nullable T value;
    private final Class<T> returnType;

    public LiteralExpressionSpec(final @Nullable T value) {
        this(value, null);
    }

    @SuppressWarnings("unchecked")
    public LiteralExpressionSpec(final @Nullable T value, final @Nullable String alias) {
        this.value = value;
        this.alias = alias;

        if (value != null) {
            this.returnType = (Class<T>) value.getClass();
        } else {
            this.returnType = (Class<T>) Object.class;
        }
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
