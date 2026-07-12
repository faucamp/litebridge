package org.litebridge.orm.expression;

import org.litebridge.orm.expression.function.scalar.LowerSpec;
import org.litebridge.orm.expression.function.scalar.SubstringSpec;
import org.litebridge.orm.expression.function.scalar.UpperSpec;

/**
 * String type-overriding nestable expression.
 * <p>
 * A nestable column expression that overrides the type of the result to {@code String}.
 */
public sealed interface StringTODelegateExpressionSpec
        extends DelegateExpressionSpec, TypeOverrideExpressionSpec<String>
        permits LowerSpec, SubstringSpec, UpperSpec {

    /**
     * Gets the return type override of the query result.
     * <p>
     * This implementation always returns {@code String.class}.
     *
     * @return {@code String.class}
     */
    @Override
    default Class<String> returnType() {
        return String.class;
    }
}
