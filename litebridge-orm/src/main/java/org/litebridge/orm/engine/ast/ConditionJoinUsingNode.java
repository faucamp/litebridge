package org.litebridge.orm.engine.ast;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.Objects;

/**
 * Represents a condition within a JOIN, WHERE or HAVING clause in the query AST.
 *
 * @param previous      the previous node in the chain
 * @param logicOperator the logic operator (AND/OR)
 */
public record ConditionJoinUsingNode(@Nullable QueryNode previous,
                                     LogicOperator logicOperator,
                                     @Nullable String usingColumn,
                                     @Nullable ExpressionSpec usingExpression) implements ConditionQueryNode {


    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof final ConditionJoinUsingNode that)) return false;
        return logicOperator == that.logicOperator
                && Objects.equals(previous, that.previous)
                && Objects.equals(usingColumn, that.usingColumn)
                && Objects.equals(usingExpression, that.usingExpression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(previous, logicOperator, usingColumn, usingExpression);
    }
}
