package org.litebridge.orm.api.select.dto;

import org.litebridge.orm.api.select.Condition;
import org.litebridge.orm.api.select.ConditionTerminal;

public interface DtoConditionTerminal<T> extends ConditionTerminal<T, DtoConditionTerminal<T>> {

    Condition<T, DtoConditionTerminal<T>> and(final String field);

}
