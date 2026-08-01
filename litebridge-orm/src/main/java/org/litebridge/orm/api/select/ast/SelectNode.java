package org.litebridge.orm.api.select.ast;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents a SELECT clause in the query AST.
 *
 * @param previous    the previous node in the chain
 * @param expressions the expressions to select
 * @param resultType  the target result type, if overridden
 */
public record SelectNode(@Nullable QueryNode previous,
                         ExpressionSpec[] expressions,
                         @Nullable Class<?> resultType) implements QueryNode {

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof final SelectNode that)) return false;
        return Objects.equals(previous, that.previous) && Objects.equals(resultType, that.resultType) && Objects.deepEquals(expressions, that.expressions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(previous, Arrays.hashCode(expressions), resultType);
    }
}
