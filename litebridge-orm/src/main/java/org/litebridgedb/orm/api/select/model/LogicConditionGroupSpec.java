package org.litebridgedb.orm.api.select.model;

import org.litebridgedb.db.spi.query.LogicOperator;

public record LogicConditionGroupSpec(LogicOperator logicOperator, ConditionGroupSpec conditionGroupSpec) {

    public LogicConditionGroupSpec(final LogicOperator logicOperator) {
        this(logicOperator, new ConditionGroupSpec());
    }
}
