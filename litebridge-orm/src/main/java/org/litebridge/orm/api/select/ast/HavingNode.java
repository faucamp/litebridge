package org.litebridge.orm.api.select.ast;

import org.jspecify.annotations.Nullable;

/**
 * Represents a HAVING clause condition in the query AST.
 *
 * @param previous  the previous node in the chain
 * @param condition the last embedded condition node for this node
 */
public record HavingNode(@Nullable QueryNode previous, QueryNode condition) implements QueryNode {
}
