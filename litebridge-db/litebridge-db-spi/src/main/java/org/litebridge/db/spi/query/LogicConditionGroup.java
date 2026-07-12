package org.litebridge.db.spi.query;

/**
 * Logically combined condition subgroup.
 *
 * @param logicOperator  The logical operator used to combine this condition with the previous one.
 * @param conditionGroup The condition group itself.
 */
public record LogicConditionGroup(LogicOperator logicOperator, ConditionGroup conditionGroup) {
}
