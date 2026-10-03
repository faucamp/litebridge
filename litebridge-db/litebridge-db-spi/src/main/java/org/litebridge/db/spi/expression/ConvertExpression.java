package org.litebridge.db.spi.expression;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Operation;

/**
 * Represents a conversion expression that wraps another {@code SelectExpression},
 * potentially overriding its type.
 * <p>
 * This class serves as a decorator around a target {@code SelectExpression}, allowing
 * for an optional type override. It delegates SQL generation to the encapsulated target
 * expression.
 *
 * @param target       The encapsulated target column expression for this expression.
 * @param typeOverride The class type that overrides the default type.
 */
public record ConvertExpression(SelectExpression target, Class<?> typeOverride) implements DelegateExpression {

    @Override
    public String toSql(final Operation operation, final ClauseType clause, final @Nullable DelegateExpression parent) {
        return target.toSql(operation, clause, parent);
    }
}
