package org.litebridge.orm.engine.ast;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.LogicOperator;

/**
 * Represents a group of conditions in the query AST.
 *
 * @param previous      the previous node in the chain
 * @param logicOperator the logic operator (AND/OR) for this group
 * @param lastChild     the last child node in the group
 */
public record ConditionGroupNode(@Nullable QueryNode previous,
                                 LogicOperator logicOperator,
                                 QueryNode lastChild) implements ConditionQueryNode {
}
