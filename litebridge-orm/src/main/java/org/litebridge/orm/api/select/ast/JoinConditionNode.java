package org.litebridge.orm.api.select.ast;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.orm.expression.ExpressionSpec;

/**
 * Represents a condition within a JOIN clause in the query AST.
 *
 * @param previous      the previous node in the chain
 * @param logicOperator the logic operator (AND/OR)
 * @param lhs           the left-hand side expression
 * @param operator      the operator (EQ, USING, etc.)
 * @param rhs           the right-hand side value
 */
public record JoinConditionNode(@Nullable QueryNode previous,
                                LogicOperator logicOperator,
                                @Nullable ExpressionSpec lhs,
                                Operator operator,
                                @Nullable Object rhs,
                                @Nullable String relationshipField) implements QueryNode {

    public JoinConditionNode(@Nullable QueryNode previous,
                             LogicOperator logicOperator,
                             @Nullable ExpressionSpec lhs,
                             Operator operator,
                             @Nullable Object rhs) {
        this(previous, logicOperator, lhs, operator, rhs, null);
    }
}
