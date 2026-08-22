package org.litebridge.orm.api.select.ast;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Table;

/**
 * Represents a DELETE statement in the query AST.
 *
 * @param previous the previous node in the chain
 * @param table    the table to delete from
 */
public record DeleteNode(@Nullable QueryNode previous,
                         @Nullable String table,
                         @Nullable Class<?> dtoClass) implements QueryNode {
}
