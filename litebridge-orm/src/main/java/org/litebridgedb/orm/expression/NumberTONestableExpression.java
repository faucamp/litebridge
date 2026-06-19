package org.litebridgedb.orm.expression;

import org.litebridgedb.orm.expression.function.scalar.AbsSpec;
import org.litebridgedb.orm.expression.function.scalar.LowerSpec;
import org.litebridgedb.orm.expression.function.scalar.SubstringSpec;
import org.litebridgedb.orm.expression.function.scalar.UpperSpec;

/**
 * String Type Override Nestable Expression.
 * <p>
 * A nestable column expression that overrides the type of the result to {@code String}.
 */
public sealed interface NumberTONestableExpression
        extends NestableExpression, TypeOverrideExpression<Number>
        permits AbsSpec {

    /**
     * Gets the return type override of the query result.
     * <p>
     * This implementation always returns {@code String.class}.
     *
     * @return {@code String.class}
     */
    @Override
    default Class<Number> returnType() {
        return Number.class;
    }
}
