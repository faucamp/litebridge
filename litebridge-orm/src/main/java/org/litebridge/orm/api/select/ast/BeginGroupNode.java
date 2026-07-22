package org.litebridge.orm.api.select.ast;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.LogicOperator;

/**
 * Represents the beginning of a condition group in the query AST.
 *
 * @param previous      the previous node in the chain
 * @param logicOperator the logic operator for the group
 */
public record BeginGroupNode(@Nullable QueryNode previous, LogicOperator logicOperator) implements QueryNode {
}
