package org.litebridge.orm.persistence;

import org.litebridge.db.api.query.Operator;

import java.util.List;
import java.util.stream.Stream;

public class Condition<T> implements org.litebridge.db.api.query.Condition {

    private final Selector<T>.SelectorStack selectorStack;
    private final String column;
    private Operator operator;
    private Object operand;

    public Condition(final String column, final Selector<T>.SelectorStack selectorStack) {
        this.column = column;
        this.selectorStack = selectorStack;
        selectorStack.push(this);
    }

    private ConditionClosure condition(final Operator eq, final Object value) {
        this.operator = eq;
        this.operand = value;
        return new ConditionClosure();
    }

    public Condition<T>.ConditionClosure eq(final Object value) {
        return condition(Operator.EQ, value);
    }

    public Condition<T>.ConditionClosure neq(final Object value) {
        return condition(Operator.NEQ, value);
    }

    public Condition<T>.ConditionClosure lt(final Object value) {
        return condition(Operator.LT, value);
    }

    public Condition<T>.ConditionClosure lte(final Object value) {
        return condition(Operator.LTE, value);
    }

    public Condition<T>.ConditionClosure gt(final Object value) {
        return condition(Operator.GT, value);
    }

    public Condition<T>.ConditionClosure gte(final Object value) {
        return condition(Operator.GTE, value);
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

    public class ConditionClosure {

        public Condition<T> and(final String field) {
            return selectorStack.where(field);
        }

        public T get() {
            return selectorStack.get();
        }

        public List<T> getAll() {
            return selectorStack.getAll();
        }

        public Stream<T> stream() {
            return selectorStack.stream();
        }
    }
}
