package org.litebridge.orm.persistence;

import jakarta.annotation.Nullable;
import org.litebridge.db.api.query.Operator;

/**
 * Represents a condition in a query, encapsulating column, operator, and operand.
 */
public class Condition<T> extends DelegatingSelectorChain<T> implements org.litebridge.db.api.query.Condition, ConditionClosure<T> {

    private final String column;
    private Operator operator;
    private Object operand;

    public Condition(final String column, final Selector<T> selector) {
        super(selector);
        this.column = column;
    }

    /**
     * Creates a condition closure for the specified operator and value.
     *
     * @param operator The operator for the condition.
     * @param value    The operand for the condition.
     * @return A ConditionClosure instance for further chaining.
     */
    private ConditionClosure<T> condition(final Operator operator, final Object value) {
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

        return this;
    }

    /**
     * Creates a condition closure for equality comparison with the specified value.
     *
     * @param value The operand for the condition.
     * @return A ConditionClosure instance for further chaining.
     */
    public ConditionClosure<T> eq(final @Nullable Object value) {
        return condition(Operator.EQ, value);
    }

    /**
     * Creates a condition closure for inequality comparison with the specified value.
     *
     * @param value The operand for the condition.
     * @return A ConditionClosure instance for further chaining.
     */
    public ConditionClosure<T> neq(final @Nullable Object value) {
        return condition(Operator.NEQ, value);
    }

    /**
     * Creates a condition closure for less-than comparison with the specified value.
     *
     * @param value The operand for the condition.
     * @return A ConditionClosure instance for further chaining.
     */
    public ConditionClosure<T> lt(final Object value) {
        return condition(Operator.LT, value);
    }

    /**
     * Creates a condition closure for less-than-or-equal comparison with the specified value.
     *
     * @param value The operand for the condition.
     * @return A ConditionClosure instance for further chaining.
     */
    public ConditionClosure<T> lte(final Object value) {
        return condition(Operator.LTE, value);
    }

    /**
     * Creates a condition closure for greater-than comparison with the specified value.
     *
     * @param value The operand for the condition.
     * @return A ConditionClosure instance for further chaining.
     */
    public ConditionClosure<T> gt(final Object value) {
        return condition(Operator.GT, value);
    }

    /**
     * Creates a condition closure for greater-than-or-equal comparison with the specified value.
     *
     * @param value The operand for the condition.
     * @return A ConditionClosure instance for further chaining.
     */
    public ConditionClosure<T> gte(final Object value) {
        return condition(Operator.GTE, value);
    }

    /**
     * Creates a condition closure for null comparison.
     * Equivalent to {@code eq(null)}.
     *
     * @return A ConditionClosure instance for further chaining.
     */
    public ConditionClosure<T> isNull() {
        return condition(Operator.IS_NULL, null);
    }

    /**
     * Creates a condition closure for not null comparison.
     * Equivalent to {@code neq(null)}.
     *
     * @return A ConditionClosure instance for further chaining.
     */
    public ConditionClosure<T> isNotNull() {
        return condition(Operator.IS_NOT_NULL, null);
    }

    @Override
    public Condition<T> and(final String field) {
        return selector.where(field);
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
