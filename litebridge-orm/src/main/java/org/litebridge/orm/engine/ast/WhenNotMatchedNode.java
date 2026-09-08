package org.litebridge.orm.engine.ast;

import org.jspecify.annotations.Nullable;

/**
 * Represents a WHEN NOT MATCHED clause in a MERGE statement in the query AST.
 *
 * @param previous the previous node in the chain
 * @param and      AND condition to apply to the WHEN NOT MATCHED clause
 * @param insert   INSERT clause to apply to the WHEN NOT MATCHED clause
 */
public record WhenNotMatchedNode(QueryNode previous,
                                 @Nullable QueryNode and,
                                 QueryNode insert) implements QueryNode {
}
