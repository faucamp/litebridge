package org.litebridge.orm.api.select.model;

import org.litebridge.db.spi.query.LogicOperator;

/**
 * Specification for a logical condition.
 * @param logicOperator the logical operator.
 * @param conditionSpec the condition specification.
 */
public record LogicConditionSpec(LogicOperator logicOperator, ConditionSpec conditionSpec) {
}
