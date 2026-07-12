package org.litebridge.orm.api.select.impl;

import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.select.model.ConditionGroupSpec;
import org.litebridge.orm.api.select.model.SelectExpressionMapper;

import java.util.ArrayDeque;
import java.util.Deque;

public abstract class AbstractConditionBasedSpec {

    protected final Table table;
    protected final ConditionGroupSpec conditions = new ConditionGroupSpec();
    protected final SelectExpressionMapper selectExpressionMapper;
    private final Deque<ConditionGroupSpec> conditionGroupStack = new ArrayDeque<>();

    public AbstractConditionBasedSpec(final Table table, final SelectExpressionMapper selectExpressionMapper) {
        this.table = table;
        this.selectExpressionMapper = selectExpressionMapper;
    }

    public ConditionGroupSpec currentConditionGroupSpec() {
        if (conditionGroupStack.isEmpty()) {
            return conditions;
        }

        return conditionGroupStack.peek();
    }

    public ConditionGroupSpec pushConditionGroupSpec(final LogicOperator logicOperator) {
        final ConditionGroupSpec subgroup = currentConditionGroupSpec().newSubgroup(logicOperator).conditionGroupSpec();
        conditionGroupStack.push(subgroup);
        return subgroup;
    }

    public void popConditionGroupSpec() {
        conditionGroupStack.pop();
    }

    public Table table() {
        return table;
    }
}
