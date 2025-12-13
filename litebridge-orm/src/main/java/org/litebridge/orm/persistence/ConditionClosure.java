package org.litebridge.orm.persistence;

public interface ConditionClosure<T> extends SelectorChain<T> {

    Condition<T> and(final String field);
}
