package org.litebridge.orm.api.select.model;

import org.litebridge.db.spi.query.LogicOperator;

/**
 * Specification for a logical condition group.
 * @param logicOperator the logical operator.
 * @param conditionGroupSpec the condition group specification.
 */
public record LogicConditionGroupSpec(LogicOperator logicOperator, ConditionGroupSpec conditionGroupSpec) {

    /**
     * Constructs a new {@code LogicConditionGroupSpec} with the specified logical operator.
     * @param logicOperator the logical operator.
     */
    public LogicConditionGroupSpec(final LogicOperator logicOperator) {
        this(logicOperator, new ConditionGroupSpec());
    }
}
