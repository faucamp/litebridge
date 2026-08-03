package org.litebridge.db.spi.expression;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.PreparedOperation;

/**
 * Represents a conversion expression that wraps another {@code SelectExpression},
 * potentially overriding its type.
 * <p>
 * This class serves as a decorator around a target {@code SelectExpression}, allowing
 * for an optional type override. It delegates SQL generation to the encapsulated target
 * expression.
 */
public class ConvertExpression implements DelegateExpression {

    private final SelectExpression target;
    private final Class<?> typeOverride;

    /**
     * Constructor.
     *
     * @param target       The encapsulated target column expression for this expression.
     * @param typeOverride The class type that overrides the default type.
     */
    public ConvertExpression(final SelectExpression target, final Class<?> typeOverride) {
        this.target = target;
        this.typeOverride = typeOverride;
    }

    @Override
    public String toSql(final Operation operation, final ClauseType clause, final @Nullable DelegateExpression parent) {
        return target.toSql(operation, clause, parent);
    }

    @Override
    public SelectExpression target() {
        return target;
    }

    /**
     * Retrieves the class type that overrides the default type of the wrapped {@code SelectExpression}.
     *
     * @return The type override, or {@code null} if no override is specified.
     */
    public Class<?> typeOverride() {
        return typeOverride;
    }
}
