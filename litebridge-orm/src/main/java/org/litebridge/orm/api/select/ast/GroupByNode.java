package org.litebridge.orm.api.select.ast;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.expression.ExpressionSpec;

/**
 * Represents a GROUP BY clause in the query AST.
 *
 * @param previous    the previous node in the chain
 * @param expressions the expressions to group by
 */
public record GroupByNode(@Nullable QueryNode previous,
                          String @Nullable [] columns,
                          ExpressionSpec @Nullable [] expressions) implements QueryNode {
}
