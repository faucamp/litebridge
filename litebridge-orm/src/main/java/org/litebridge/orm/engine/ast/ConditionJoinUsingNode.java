package org.litebridge.orm.engine.ast;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.Objects;

/**
 * Represents a JOIN USING condition in the query AST.
 *
 * @param previous        the previous node in the chain
 * @param logicOperator   the logic operator (AND/OR)
 * @param usingColumn     the column to join on
 * @param usingExpression the expression to join on
 */
public record ConditionJoinUsingNode(@Nullable QueryNode previous,
                                     LogicOperator logicOperator,
                                     @Nullable String usingColumn,
                                     @Nullable ExpressionSpec usingExpression) implements ConditionQueryNode {

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof ConditionJoinUsingNode(
                QueryNode previous1, LogicOperator operator, String column, ExpressionSpec expression
        ))) return false;
        return logicOperator == operator
                && Objects.equals(previous, previous1)
                && Objects.equals(usingColumn, column)
                && Objects.equals(usingExpression, expression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(previous, logicOperator, usingColumn, usingExpression);
    }
}
