package org.litebridge.orm.expression.select;

import org.litebridge.orm.expression.AbstractAliasable;
import org.litebridge.orm.expression.TypeOverrideExpressionSpec;

public final class LiteralExpressionSpec<T> extends AbstractAliasable implements TypeOverrideExpressionSpec<T> {

    private final T value;
    private final Class<T> returnType;

    @SuppressWarnings("unchecked")
    public LiteralExpressionSpec(final T value) {
        this.value = value;
        this.returnType = (Class<T>) value.getClass();
    }

    @Override
    public Class<T> returnType() {
        return null;
    }
}
