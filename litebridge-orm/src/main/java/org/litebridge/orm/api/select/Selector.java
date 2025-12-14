package org.litebridge.orm.api.select;

public interface Selector<T, CT extends ConditionTerminal<T, CT>> extends SelectorChain<T, CT> {

    Condition<T, CT> where(final String column);
}
