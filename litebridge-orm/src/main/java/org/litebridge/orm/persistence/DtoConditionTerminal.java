package org.litebridge.orm.persistence;

public interface DtoConditionTerminal<T> extends ConditionTerminal<T, DtoConditionTerminal<T>> {

    Condition<T, DtoConditionTerminal<T>> and(final String field);

}
