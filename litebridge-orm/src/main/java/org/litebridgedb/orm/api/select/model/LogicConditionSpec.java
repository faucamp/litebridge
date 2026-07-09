package org.litebridgedb.orm.api.select.model;

import org.litebridgedb.db.spi.query.LogicOperator;

public record LogicConditionSpec(LogicOperator logicOperator, ConditionSpec conditionSpec) {
}
