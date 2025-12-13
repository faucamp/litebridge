package org.litebridge.orm.persistence;

public interface Selector<T> extends SelectorChain<T> {

    Condition<T> where(final String column);
}
