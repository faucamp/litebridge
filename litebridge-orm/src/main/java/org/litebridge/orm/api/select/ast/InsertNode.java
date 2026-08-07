package org.litebridge.orm.api.select.ast;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Table;

/**
 * Represents an INSERT statement in the query AST.
 *
 * @param previous the previous node in the chain
 * @param table    the table to insert into
 */
public record InsertNode(@Nullable QueryNode previous, Table table) implements QueryNode {
}
