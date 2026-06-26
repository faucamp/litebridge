package org.litebridgedb.orm.expression;

import org.litebridgedb.orm.expression.function.scalar.AbsSpec;

/**
 * Number type-overriding nestable expression
 * <p>
 * A nestable lhs expression that overrides the type of the result to {@code Number}.
 */
public sealed interface NumberTONestableExpressionSpec
        extends NestableExpressionSpec, TypeOverrideExpressionSpec<Number>
        permits AbsSpec {

    /**
     * Gets the return type override of the query result.
     * <p>
     * This implementation always returns {@code Number.class}.
     *
     * @return {@code Number.class}
     */
    @Override
    default Class<Number> returnType() {
        return Number.class;
    }
}
