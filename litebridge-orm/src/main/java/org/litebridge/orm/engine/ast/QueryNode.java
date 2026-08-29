package org.litebridge.orm.engine.ast;

import org.jspecify.annotations.Nullable;

/**
 * A node in a lightweight AST representing a step in a fluent query chain.
 */
public sealed interface QueryNode

        permits ConditionQueryNode, DeleteNode, GroupByNode, HavingNode, InsertNode, InsertValuesNode, JoinNode, LimitNode, MergeNode, OrderByNode, SelectNode, SetNode, UpdateNode, UsingNode, WhenMatchedNode, WhenNotMatchedNode, WhereNode {

    /**
     * Returns the previous node in the chain, or {@code null} if this is the root node.
     *
     * @return the previous node
     */
    @Nullable QueryNode previous();
}
