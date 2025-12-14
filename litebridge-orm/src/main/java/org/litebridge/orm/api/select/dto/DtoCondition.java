package org.litebridge.orm.api.select.dto;

import org.litebridge.orm.api.select.Condition;
import org.litebridge.orm.api.select.Selector;

/**
 * Represents a condition in a query, encapsulating column, operator, and operand.
 */
public class DtoCondition<T> extends Condition<T, DtoConditionTerminal<T>> {

    public DtoCondition(final String column, final Selector<T, DtoConditionTerminal<T>> selector, final DtoConditionTerminal<T> conditionTerminal) {
        super(column, selector, conditionTerminal);
    }
}
