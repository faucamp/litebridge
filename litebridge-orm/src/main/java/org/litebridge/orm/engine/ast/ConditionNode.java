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
 * @param previous      the previous node in the chain
 * @param logicOperator the logic operator (AND/OR)
 * @param lhsColumn     the left-hand side column name
 * @param lhsExpression the left-hand side expression
 * @param operator      the operator (EQ, USING, etc.)
 * @param rhs           the right-hand side value
 * @param rhsColumn     the right-hand side column name
 */
public record ConditionNode(@Nullable QueryNode previous,
                            LogicOperator logicOperator,
                            @Nullable String lhsColumn,
                            @Nullable ExpressionSpec lhsExpression,
                            Operator operator,
                            @Nullable Object rhs,
                            @Nullable String rhsColumn) implements ConditionQueryNode {

    /**
     * Creates a new {@code ConditionNode} instance without a right-hand side column.
     *
     * @param previous      the previous node in the chain
     * @param logicOperator the logic operator (AND/OR)
     * @param lhsColumn     the left-hand side column name
     * @param lhsExpression the left-hand side expression
     * @param operator      the operator (EQ, USING, etc.)
     * @param rhs           the right-hand side value
     */
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
        if (!(o instanceof ConditionNode(
                QueryNode previous1, LogicOperator logicOperator1, String column, ExpressionSpec expression,
                Operator operator1, Object rhs1, String rhsColumn1
        ))) return false;
        return operator == operator1
                && Objects.equals(previous, previous1)
                && Objects.equals(lhsColumn, column)
                && Objects.equals(lhsExpression, expression)
                && Objects.equals(rhsColumn, rhsColumn1)
                && logicOperator == logicOperator1
                && Objects.equals(valueStructuralKey(rhs), valueStructuralKey(rhs1));
    }

    @Override
    public int hashCode() {
        return Objects.hash(previous, logicOperator, lhsColumn, lhsExpression, operator, rhsColumn, valueStructuralKey(rhs));
    }
}
