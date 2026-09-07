package org.litebridge.orm.engine.ast;

/**
 * An AST node representing a condition.
 */
public sealed interface ConditionQueryNode
        extends QueryNode
        permits ConditionJoinUsingNode, ConditionGroupNode, ConditionNode, ConditionWithIdNode {
}
