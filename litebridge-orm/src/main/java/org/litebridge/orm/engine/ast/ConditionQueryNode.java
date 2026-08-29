package org.litebridge.orm.engine.ast;

/**
 * A node in a lightweight AST representing a step in a fluent query chain.
 */
public sealed interface ConditionQueryNode
        extends QueryNode
        permits ConditionJoinUsingNode, ConditionGroupNode, ConditionNode, ConditionWithIdNode {
}
