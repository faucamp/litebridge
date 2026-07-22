package org.litebridge.orm.api.select.ast;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.expression.ExpressionSpec;

/**
 * Represents a SELECT clause in the query AST.
 *
 * @param previous    the previous node in the chain
 * @param expressions the expressions to select
 * @param resultType  the target result type, if overridden
 */
public record SelectNode(@Nullable QueryNode previous, ExpressionSpec[] expressions, @Nullable Class<?> resultType) implements QueryNode {
}
