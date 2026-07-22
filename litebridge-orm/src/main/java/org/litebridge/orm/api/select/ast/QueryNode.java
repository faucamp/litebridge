package org.litebridge.orm.api.select.ast;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.orm.config.RelatedDtoStrategy;
import org.litebridge.orm.expression.ExpressionSpec;

/**
 * A node in a lightweight AST representing a step in a fluent query chain.
 */
public sealed interface QueryNode permits
        SelectNode,
        FromNode,
        JoinNode,
        JoinConditionNode,
        WhereNode,
        BeginGroupNode,
        EndGroupNode,
        GroupByNode,
        HavingNode,
        OrderByNode,
        LimitNode {

    /**
     * Returns the previous node in the chain, or {@code null} if this is the root node.
     *
     * @return the previous node
     */
    @Nullable QueryNode previous();
}
