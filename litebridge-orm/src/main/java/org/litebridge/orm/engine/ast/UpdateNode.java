package org.litebridge.orm.engine.ast;

import org.jspecify.annotations.Nullable;

/**
 * Represents an UPDATE statement in the query AST.
 *
 * @param previous the previous node in the chain
 * @param table    the table to update
 */
public record UpdateNode(@Nullable QueryNode previous,
                         @Nullable String table,
                         @Nullable Class<?> dtoClass) implements QueryNode {
}
