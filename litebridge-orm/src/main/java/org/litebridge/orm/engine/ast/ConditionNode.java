package org.litebridge.orm.engine.ast;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.Objects;

import static org.litebridge.orm.engine.ast.ConditionNodeUtil.valueStructuralKey;

/**
 * Represents a condition within a JOIN, WHERE or HAVING clause in the query AST.
 *
 * @param previous          the previous node in the chain
 * @param logicOperator     the logic operator (AND/OR)
 * @param lhs               the left-hand side expression
 * @param operator          the operator (EQ, USING, etc.)
 * @param rhs               the right-hand side value
 */
public record ConditionNode(@Nullable QueryNode previous,
                            LogicOperator logicOperator,
                            @Nullable String lhsColumn,
                            @Nullable ExpressionSpec lhsExpression,
                            Operator operator,
                            @Nullable Object rhs,
                            @Nullable String rhsColumn) implements ConditionQueryNode {

    public ConditionNode(@Nullable QueryNode previous,
                         LogicOperator logicOperator,
                         @Nullable String lhsColumn,
                         @Nullable ExpressionSpec lhsExpression,
                         Operator operator,
                         @Nullable Object rhs) {
        this(previous, logicOperator, lhsColumn, lhsExpression, operator, rhs, null);
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof final ConditionNode that)) return false;
        return operator == that.operator
                && Objects.equals(previous, that.previous)
                && Objects.equals(lhsColumn, that.lhsColumn)
                && Objects.equals(lhsExpression, that.lhsExpression)
                && Objects.equals(rhsColumn, that.rhsColumn)
                && logicOperator == that.logicOperator
                && Objects.equals(valueStructuralKey(rhs), valueStructuralKey(that.rhs));
    }

    @Override
    public int hashCode() {
        return Objects.hash(previous, logicOperator, lhsColumn, lhsExpression, operator, rhsColumn, valueStructuralKey(rhs));
    }
}
