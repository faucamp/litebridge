package org.litebridge.orm.api.select.model;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.StringJoiner;

/**
 * Specification for a condition in a database query.
 * <p>
 * This is used in a SQL query WHERE clause, JOIN clause, etc.
 * <p>
 * A condition consists of a column, an operator, and an optional value. The operator
 * dictates how the column will be compared to the provided value.
 */
public class ConditionSpec {

    private @Nullable String lhsColumn;
    private @Nullable ExpressionSpec lhsExpression;
    private @Nullable Operator operator;
    private @Nullable Object value;

    /**
     * Constructs a new {@code ConditionSpec} with specified components.
     *
     * @param lhsColumn     the left-hand side column name
     * @param lhsExpression the left-hand side expression
     * @param operator      the condition operator
     * @param rawValue      the raw right-hand side value
     */
    public ConditionSpec(final @Nullable String lhsColumn,
                         final @Nullable ExpressionSpec lhsExpression,
                         final @Nullable Operator operator,
                         final @Nullable Object rawValue) {
        this.lhsColumn = lhsColumn;
        this.lhsExpression = lhsExpression;
        this.operator = operator;
        this.value = rawValue;
    }

    /**
     * Constructs an empty {@code ConditionSpec}.
     */
    public ConditionSpec() {
    }

    /**
     * Gets the left-hand side column name of the condition.
     *
     * @return the LHS column name, or {@code null}
     */
    public @Nullable String getLhsColumn() {
        return lhsColumn;
    }

    /**
     * Sets the left-hand side column name of the condition.
     *
     * @param lhsColumn the LHS column name to set
     */
    public void setLhsColumn(final String lhsColumn) {
        this.lhsColumn = lhsColumn;
    }

    /**
     * Gets the left-hand side expression of the condition.
     *
     * @return the LHS expression
     */
    public @Nullable ExpressionSpec getLhsExpression() {
        return lhsExpression;
    }

    /**
     * Sets the left-hand side expression of the condition.
     *
     * @param lhs the LHS expression to set
     */
    public void setLhsExpression(final ExpressionSpec lhs) {
        this.lhsExpression = lhsExpression;
    }

    /**
     * Gets the operator used in the condition.
     *
     * @return the operator
     */
    public Operator getOperator() {
        return operator;
    }

    /**
     * Sets the operator for the condition.
     *
     * @param operator the operator to set
     */
    public void setOperator(final Operator operator) {
        this.operator = operator;
    }

    /**
     * Gets the right-hand side value of the condition.
     *
     * @return the RHS value, or {@code null}
     */
    public @Nullable Object getValue() {
        return value;
    }

    /**
     * Sets the right-hand side value for the condition.
     *
     * @param value the RHS value to set
     */
    public void setValue(final @Nullable Object value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ConditionSpec.class.getSimpleName() + "[", "]")
                .add("lhsColumn=" + lhsColumn)
                .add("lhsExpression=" + lhsExpression)
                .add("operator=" + operator)
                .add("value=" + value)
                .toString();
    }
}
