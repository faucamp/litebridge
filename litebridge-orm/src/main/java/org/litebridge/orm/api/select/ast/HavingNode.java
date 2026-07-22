package org.litebridge.orm.api.select.ast;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.orm.expression.ExpressionSpec;

/**
 * Represents a HAVING clause condition in the query AST.
 *
 * @param previous      the previous node in the chain
 * @param logicOperator the logic operator (AND/OR)
 * @param lhs           the left-hand side expression
 * @param operator      the operator (EQ, GT, etc.)
 * @param rhs           the right-hand side value
 */
public record HavingNode(@Nullable QueryNode previous, LogicOperator logicOperator, ExpressionSpec lhs, Operator operator, @Nullable Object rhs) implements QueryNode {
}
