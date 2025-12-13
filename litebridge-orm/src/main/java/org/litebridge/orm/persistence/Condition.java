package org.litebridge.orm.persistence;

import jakarta.annotation.Nullable;
import org.litebridge.db.api.query.Operator;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Represents a condition in a query, encapsulating column, operator, and operand.
 */
public class Condition<T> implements org.litebridge.db.api.query.Condition {

    private final AbstractSelector<T>.SelectorStack selectorStack;
    private final String column;
    private Operator operator;
    private Object operand;

    public Condition(final String column, final AbstractSelector<T>.SelectorStack selectorStack) {
        this.column = column;
        this.selectorStack = selectorStack;
        selectorStack.push(this);
    }

    /**
     * Creates a condition closure for the specified operator and value.
     *
     * @param operator The operator for the condition.
     * @param value    The operand for the condition.
     * @return A ConditionClosure instance for further chaining.
     */
    private ConditionClosure condition(final Operator operator, final Object value) {
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

        return new ConditionClosure();
    }

    /**
     * Creates a condition closure for equality comparison with the specified value.
     *
     * @param value The operand for the condition.
     * @return A ConditionClosure instance for further chaining.
     */
    public Condition<T>.ConditionClosure eq(final @Nullable Object value) {
        return condition(Operator.EQ, value);
    }

    /**
     * Creates a condition closure for inequality comparison with the specified value.
     *
     * @param value The operand for the condition.
     * @return A ConditionClosure instance for further chaining.
     */
    public Condition<T>.ConditionClosure neq(final @Nullable Object value) {
        return condition(Operator.NEQ, value);
    }

    /**
     * Creates a condition closure for less-than comparison with the specified value.
     *
     * @param value The operand for the condition.
     * @return A ConditionClosure instance for further chaining.
     */
    public Condition<T>.ConditionClosure lt(final Object value) {
        return condition(Operator.LT, value);
    }

    /**
     * Creates a condition closure for less-than-or-equal comparison with the specified value.
     *
     * @param value The operand for the condition.
     * @return A ConditionClosure instance for further chaining.
     */
    public Condition<T>.ConditionClosure lte(final Object value) {
        return condition(Operator.LTE, value);
    }

    /**
     * Creates a condition closure for greater-than comparison with the specified value.
     *
     * @param value The operand for the condition.
     * @return A ConditionClosure instance for further chaining.
     */
    public Condition<T>.ConditionClosure gt(final Object value) {
        return condition(Operator.GT, value);
    }

    /**
     * Creates a condition closure for greater-than-or-equal comparison with the specified value.
     *
     * @param value The operand for the condition.
     * @return A ConditionClosure instance for further chaining.
     */
    public Condition<T>.ConditionClosure gte(final Object value) {
        return condition(Operator.GTE, value);
    }

    /**
     * Creates a condition closure for null comparison.
     * Equivalent to {@code eq(null)}.
     *
     * @return A ConditionClosure instance for further chaining.
     */
    public Condition<T>.ConditionClosure isNull() {
        return condition(Operator.IS_NULL, null);
    }

    /**
     * Creates a condition closure for not null comparison.
     * Equivalent to {@code neq(null)}.
     *
     * @return A ConditionClosure instance for further chaining.
     */
    public Condition<T>.ConditionClosure isNotNull() {
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

    /**
     * Represents a closure for chaining additional conditions.
     */
    public class ConditionClosure {

        /**
         * Chains an additional condition with the specified DTO field.
         *
         * @param field The DTO field name to apply the condition to.
         * @return A Condition instance for further chaining.
         */
        public Condition<T> and(final String field) {
            return selectorStack.where(field);
        }

        /**
         * Returns a single matching DTO from the query result.
         *
         * @return a single matching DTO from the query result
         * @throws IllegalStateException if the query result contains more than one matching DTO
         */
        public Optional<T> one() {
            return selectorStack.one();
        }

        public T oneOrNull() {
            return selectorStack.oneOrNull();
        }

        public T oneOrThrow() {
            return selectorStack.oneOrThrow();
        }

        public <X extends Throwable> T oneOrThrow(final Supplier<? extends X> exceptionSupplier) throws X {
            return selectorStack.oneOrThrow(exceptionSupplier);
        }

        public Optional<T> first() {
            return selectorStack.first();
        }

        public T firstOrNull() {
            return selectorStack.firstOrNull();
        }

        public T firstOrThrow() {
            return selectorStack.firstOrThrow();
        }

        public <X extends Throwable> T firstOrThrow(final Supplier<? extends X> exceptionSupplier) throws X {
            return selectorStack.firstOrThrow(exceptionSupplier);
        }

        /**
         * Returns all matching DTOs from the query result.
         *
         * @return a list of matching DTOs from the query result
         */
        public List<T> list() {
            return selectorStack.list();
        }

        /**
         * Returns a stream of matching DTOs from the query result.
         *
         * @return a stream of matching DTOs from the query result
         */
        public Stream<T> stream() {
            return selectorStack.stream();
        }
    }
}
