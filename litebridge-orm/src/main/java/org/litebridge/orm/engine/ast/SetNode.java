package org.litebridge.orm.engine.ast;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.math.MathOperator;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.Objects;

/**
 * Represents a SET clause in an UPDATE statement in the query AST.
 *
 * @param previous       the previous node in the chain
 * @param column         the column to update
 * @param expressionSpec the expression to use instead of the column
 * @param value          the value to set (or use in the math operation, if specified)
 * @param mathOperator   optional math operator to use when setting the value
 */
public record SetNode(@Nullable QueryNode previous,
                      @Nullable String column,
                      @Nullable ExpressionSpec expressionSpec,
                      @Nullable Object value,
                      @Nullable MathOperator mathOperator) implements QueryNode {

    public SetNode(@Nullable QueryNode previous, String column, @Nullable Object value) {
        this(previous, column, null, value, null);
    }

    public SetNode(@Nullable QueryNode previous, ExpressionSpec expressionSpec, @Nullable Object value) {
        this(previous, null, expressionSpec, value, null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SetNode setNode)) return false;
        return Objects.equals(previous, setNode.previous)
                && Objects.equals(column, setNode.column)
                && Objects.equals(expressionSpec, setNode.expressionSpec)
                && Objects.equals(mathOperator, setNode.mathOperator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(previous, column, expressionSpec, mathOperator);
    }
}
