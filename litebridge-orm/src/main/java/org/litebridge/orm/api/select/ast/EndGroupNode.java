package org.litebridge.orm.api.select.ast;

import org.jspecify.annotations.Nullable;

/**
 * Represents the end of a condition group in the query AST.
 *
 * @param previous the previous node in the chain
 */
public record EndGroupNode(@Nullable QueryNode previous) implements QueryNode {
}
