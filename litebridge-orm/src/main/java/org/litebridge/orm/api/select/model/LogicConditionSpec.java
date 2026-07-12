package org.litebridge.orm.api.select.model;

import org.litebridge.db.spi.query.LogicOperator;

public record LogicConditionSpec(LogicOperator logicOperator, ConditionSpec conditionSpec) {
}
