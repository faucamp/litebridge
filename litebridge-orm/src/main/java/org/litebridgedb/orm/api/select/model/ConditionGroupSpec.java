package org.litebridgedb.orm.api.select.model;

import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.query.ConditionGroup;
import org.litebridgedb.db.spi.query.LogicOperator;
import org.litebridgedb.orm.expression.ExpressionSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Groups conditions and condition subgroups using a logical operator.
 *
 * @param logicOperator The logical operator used to combine conditions and sub-groups.
 * @param conditions    A list of conditions to be combined.
 * @param subgroups     A list of subgroups of conditions to be combined.
 */
public record ConditionGroupSpec(LogicOperator logicOperator,
                                 List<ConditionSpec> conditions,
                                 List<ConditionGroupSpec> subgroups) {

    public ConditionGroupSpec(final LogicOperator logicOperator, final List<ConditionSpec> conditions, final List<ConditionGroupSpec> subgroups) {
        this.logicOperator = logicOperator;

        if (conditions instanceof ArrayList<ConditionSpec> arrayList) {
            this.conditions = arrayList;
        } else {
            this.conditions = new ArrayList<>(conditions);
        }

        if (subgroups instanceof ArrayList<ConditionGroupSpec> arrayList) {
            this.subgroups = arrayList;
        } else {
            this.subgroups = subgroups;
        }
    }

    public ConditionGroupSpec(LogicOperator logicOperator,
                              List<ConditionSpec> conditions) {
        this(logicOperator, conditions, new ArrayList<>());
    }

    public ConditionGroupSpec(final LogicOperator logicOperator) {
        this(logicOperator, new ArrayList<>(), new ArrayList<>());
    }

    public ConditionSpec newCondition(final ExpressionSpec expressionSpec) {
        final ConditionSpec conditionSpec = new ConditionSpec();
        conditionSpec.setLhs(expressionSpec);
        conditions.add(conditionSpec);
        return conditionSpec;
    }

    public ConditionGroupSpec newSubgroup(final LogicOperator logicOperator) {
        final ConditionGroupSpec conditionGroupSpec = new ConditionGroupSpec(logicOperator);
        subgroups.add(conditionGroupSpec);
        return conditionGroupSpec;
    }

    public ConditionGroup toConditionGroup(final SelectExpressionMapper selectExpressionMapper, final Set<Table> selectedTables) {
        final List<ConditionGroup> subConditionGroups = subgroups.stream()
                .map(conditionGroupSpec -> conditionGroupSpec.toConditionGroup(selectExpressionMapper, selectedTables))
                .toList();

        return new ConditionGroup(logicOperator,
                conditions.stream()
                        .map(conditionSpec -> conditionSpec.toCondition(selectExpressionMapper, selectedTables))
                        .toList(),
                subConditionGroups);
    }
}
