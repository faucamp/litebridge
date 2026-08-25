package org.litebridge.orm.api.select.ast;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Table;

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
