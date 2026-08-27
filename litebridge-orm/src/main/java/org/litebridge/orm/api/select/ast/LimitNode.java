package org.litebridge.orm.api.select.ast;

import org.jspecify.annotations.Nullable;

/**
 * Represents a LIMIT clause in the query AST.
 *
 * @param previous the previous node in the chain
 * @param limit    the maximum number of rows to return
 * @param offset   the number of rows to skip
 */
public record LimitNode(@Nullable QueryNode previous,
                        @Nullable Integer limit,
                        @Nullable Integer offset) implements QueryNode {
}
