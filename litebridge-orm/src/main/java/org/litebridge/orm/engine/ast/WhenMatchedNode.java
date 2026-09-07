package org.litebridge.orm.engine.ast;

import org.jspecify.annotations.Nullable;

/**
 * Represents a WHEN MATCHED clause in a MERGE statement in the query AST.
 *
 * @param previous the previous node in the chain
 * @param and      AND condition to apply to the WHEN MATCHED clause
 * @param update   SET clause to apply to the WHEN MATCHED clause
 * @param delete   Whether to delete matched rows
 */
public record WhenMatchedNode(QueryNode previous,
                              @Nullable QueryNode and,
                              @Nullable SetNode update,
                              boolean delete) implements QueryNode {
}
