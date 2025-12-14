package org.litebridge.orm.api.select.sql;

import org.litebridge.orm.api.select.Condition;

import java.util.Map;

/**
 * Represents a condition in a query, encapsulating column, operator, and operand.
 */
public class SqlCondition extends Condition<Map<String, Object>, SqlConditionTerminal> {

    public SqlCondition(final String column, final SqlSelector selector) {
        super(column, selector, selector);
    }

}
