package org.litebridge.orm.api.select.ast;

import org.jspecify.annotations.Nullable;

/**
 * Represents the VALUES clause of an INSERT STATEMENT in the query AST.
 *
 * @param previous the previous node in the chain
 * @param values   values to insert
 */
public record InsertValuesNode(@Nullable QueryNode previous, Object @Nullable [] values) implements QueryNode {
}
