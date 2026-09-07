package org.litebridge.orm.engine.compiler;

import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.select.model.ConditionGroupSpec;

/**
 * Stack for managing hierarchical context structures composed of {@link ConditionGroupSpec} objects.
 * <p>
 * This class provides mechanisms to create and manage a stack-based hierarchy
 * of condition groups, where each group may have nested subgroups interconnected
 * using logical operators.
 */
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
