package org.litebridge.orm.api.select.ast;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * A node in a lightweight AST representing a step in a fluent query chain.
 */
public sealed interface QueryNode permits ConditionQueryNode, DeleteNode, GroupByNode, HavingNode, InsertNode, JoinNode, LimitNode, OrderByNode, SelectNode, SetNode, UpdateNode, WhereNode {

    /**
     * Returns the previous node in the chain, or {@code null} if this is the root node.
     *
     * @return the previous node
     */
    @Nullable QueryNode previous();
}
