package org.litebridge.orm.api.select.ast;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.expression.ExpressionSpec;

/**
 * Represents an ORDER BY clause in the query AST.
 *
 * @param previous   the previous node in the chain
 * @param expression the expression to order by
 * @param ascending  whether to sort in ascending order
 */
public record OrderByNode(@Nullable QueryNode previous, ExpressionSpec expression, boolean ascending) implements QueryNode {
}
