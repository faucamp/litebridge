package org.litebridge.orm.engine;

import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.select.model.ConditionGroupSpec;

final class ConditionGroupSpecStack extends ContextStack<ConditionGroupSpec> {

    @Override
    protected ConditionGroupSpec newRootInstance() {
        return new ConditionGroupSpec();
    }

    @Override
    protected ConditionGroupSpec newSubInstance(final LogicOperator logicOperator) {
        return current().newSubgroup(logicOperator).conditionGroupSpec();
    }
}
