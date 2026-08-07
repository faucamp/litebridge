package org.litebridge.orm.api.select.ast;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.orm.api.select.SelectTerminal;
import org.litebridge.orm.api.select.impl.SelectTerminalInspector;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.Collection;
import java.util.Objects;

/**
 * Represents a condition within a JOIN, WHERE or HAVING clause in the query AST.
 *
 * @param previous      the previous node in the chain
 * @param logicOperator the logic operator (AND/OR)
 * @param lhs           the left-hand side expression
 * @param operator          the operator (EQ, USING, etc.)
 * @param rhs               the right-hand side value
 * @param relationshipField the field name of the relationship (if any)
 */
public record ConditionNode(@Nullable QueryNode previous,
                            LogicOperator logicOperator,
                            @Nullable ExpressionSpec lhs,
                            Operator operator,
                            @Nullable Object rhs,
                            @Nullable String relationshipField) implements ConditionQueryNode {

    public ConditionNode(@Nullable QueryNode previous,
                         LogicOperator logicOperator,
                         @Nullable ExpressionSpec lhs,
                         Operator operator,
                         @Nullable Object rhs) {
        this(previous, logicOperator, lhs, operator, rhs, null);
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof final ConditionNode that)) return false;
        return operator == that.operator
                && Objects.equals(previous, that.previous)
                && Objects.equals(lhs, that.lhs)
                && Objects.equals(relationshipField, that.relationshipField)
                && logicOperator == that.logicOperator
                && Objects.equals(rhsStructuralKey(), that.rhsStructuralKey());
    }

    @Override
    public int hashCode() {
        return Objects.hash(previous, logicOperator, lhs, operator, relationshipField, rhsStructuralKey());
    }

    private Object rhsStructuralKey() {
        if (rhs instanceof Collection<?> collection) {
            return collection.size();
        } else if (rhs instanceof QueryNode queryNode) {
            return queryNode;
        } else if (rhs instanceof SelectTerminal<?> st) {
            return SelectTerminalInspector.getNode(st);
        } else {
            return 1;
        }
    }
}
