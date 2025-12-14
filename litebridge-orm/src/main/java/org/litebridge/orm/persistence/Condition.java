package org.litebridge.orm.persistence;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.litebridge.db.api.query.Operator;

/**
 * Represents a condition in a query, encapsulating column, operator, and operand.
 */
public abstract class Condition<T, CT extends ConditionTerminal<T, CT>> extends DelegatingSelectorChain<T, CT> implements org.litebridge.db.api.query.Condition {

    @Nonnull
    private final CT conditionTerminal;
    private final String column;
    private Operator operator;
    private Object operand;

    public Condition(final String column, final Selector<T, CT> selector, final CT conditionTerminal) {
        super(selector);
        this.column = column;
        this.conditionTerminal = conditionTerminal;
    }

    /**
     * Creates a condition terminal for the specified operator and value.
     *
     * @param operator The operator for the condition.
     * @param value    The operand for the condition.
     * @return A {@link ConditionTerminal} instance for further chaining.
     */
    private CT condition(final Operator operator, final Object value) {
        this.operand = value;

        if (value == null) {
            this.operator = switch (operator) {
                case EQ -> Operator.IS_NULL;
                case NEQ -> Operator.IS_NOT_NULL;
                case IS_NULL, IS_NOT_NULL -> operator;
                default ->
                        throw new IllegalArgumentException("Operator %s does not support null value".formatted(operator));
            };
        } else {
            this.operator = operator;
        }

        return conditionTerminal;
    }

    /**
     * Equals
     *
     * @param value The operand for the condition.
     * @return A {@link ConditionTerminal} instance for further chaining.
     */
    public CT eq(final @Nullable Object value) {
        return condition(Operator.EQ, value);
    }

    /**
     * Not equals
     *
     * @param value The operand for the condition.
     * @return A {@link ConditionTerminal} instance for further chaining.
     */
    public CT neq(final @Nullable Object value) {
        return condition(Operator.NEQ, value);
    }

    /**
     * Creates a condition terminal for less-than comparison with the specified value.
     *
     * @param value The operand for the condition.
     * @return A {@link ConditionTerminal} instance for further chaining.
     */
    public CT lt(final Object value) {
        return condition(Operator.LT, value);
    }

    /**
     * Less than or equals
     *
     * @param value The operand for the condition.
     * @return A {@link ConditionTerminal} instance for further chaining.
     */
    public CT lte(final Object value) {
        return condition(Operator.LTE, value);
    }

    /**
     * Greater than
     *
     * @param value The operand for the condition.
     * @return A {@link ConditionTerminal} instance for further chaining.
     */
    public CT gt(final Object value) {
        return condition(Operator.GT, value);
    }

    /**
     * Greater than or equals
     *
     * @param value The operand for the condition.
     * @return A {@link ConditionTerminal} instance for further chaining.
     */
    public CT gte(final Object value) {
        return condition(Operator.GTE, value);
    }

    /**
     * Null comparison.
     * <p>
     * Equivalent to {@code eq(null)}.
     *
     * @return A {@link ConditionTerminal} instance for further chaining.
     */
    public CT isNull() {
        return condition(Operator.IS_NULL, null);
    }

    /**
     * Not null comparison.
     * <p>
     * Equivalent to {@code neq(null)}.
     *
     * @return A {@link ConditionTerminal} instance for further chaining.
     */
    public CT isNotNull() {
        return condition(Operator.IS_NOT_NULL, null);
    }

    @Override
    public String getColumn() {
        return column;
    }

    @Override
    public Operator getOperator() {
        return operator;
    }

    @Override
    public Object getValue() {
        return operand;
    }
}
