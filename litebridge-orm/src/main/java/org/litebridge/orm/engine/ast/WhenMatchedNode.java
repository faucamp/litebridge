package org.litebridge.orm.engine.ast;

/**
 * Represents a WHEN MATCHED clause in a MERGE statement in the query AST.
 *
 * @param previous the previous node in the chain
 * @param update   Update clause to apply to the WHEN MATCHED clause, such as UPDATE or DELETE,
 *                 optionally including WHERE clauses that will be translated to WHEN MATCHED AND clauses as necessary.
 */
public record WhenMatchedNode(QueryNode previous,
                              QueryNode update) implements QueryNode {
}
