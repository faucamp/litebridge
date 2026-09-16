package org.litebridge.orm.expression;

import org.litebridge.orm.expression.function.scalar.AbsSpec;

/**
 * Number type-overriding nestable expression
 * <p>
 * A nestable column expression that overrides the type of the result to {@code Number}.
 */
public sealed class NumberTODelegateExpressionSpec extends AbstractTODelegateExpressionSpec<Number> permits AbsSpec {

    /**
     * Creates a new {@code NumberTODelegateExpressionSpec} instance.
     *
     * @param target The target nested expression
     */
    protected NumberTODelegateExpressionSpec(final ColumnExpressionSpec target) {
        super(target, Number.class);
    }

    /**
     * Gets the return type override of the query result.
     * <p>
     * This implementation always returns {@code Number.class}.
     *
     * @return {@code Number.class}
     */
    @Override
    public Class<Number> returnType() {
        return Number.class;
    }
}
