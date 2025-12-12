package org.litebridge.orm.persistence;

import org.litebridge.db.api.query.Operator;

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

    public Condition<T>.ConditionClosure eq(final Object value) {
        this.operator = Operator.EQ;
        this.operand = value;
        return new ConditionClosure();
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
            return selectorStack.execute();
        }
    }
}
