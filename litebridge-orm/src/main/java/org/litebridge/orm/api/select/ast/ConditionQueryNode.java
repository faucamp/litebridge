package org.litebridge.orm.api.select.ast;

/**
 * A node in a lightweight AST representing a step in a fluent query chain.
 */
public sealed interface ConditionQueryNode extends QueryNode permits ConditionNode, ConditionGroupNode {
}
