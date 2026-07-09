package org.litebridgedb.orm.api.select.model;

import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.query.ConditionGroup;
import org.litebridgedb.db.spi.query.LogicCondition;
import org.litebridgedb.db.spi.query.LogicConditionGroup;
import org.litebridgedb.db.spi.query.LogicOperator;
import org.litebridgedb.orm.expression.ExpressionSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Groups conditions and condition subgroups using a logical operator.
 *
 * @param conditions A list of conditions to be combined.
 * @param subgroups  A list of subgroups of conditions to be combined.
 */
public record ConditionGroupSpec(List<LogicConditionSpec> conditions,
                                 List<LogicConditionGroupSpec> subgroups) {

    public ConditionGroupSpec(final List<LogicConditionSpec> conditions, final List<LogicConditionGroupSpec> subgroups) {
        if (conditions instanceof ArrayList<LogicConditionSpec> arrayList) {
            this.conditions = arrayList;
        } else {
            this.conditions = new ArrayList<>(conditions);
        }

        if (subgroups instanceof ArrayList<LogicConditionGroupSpec> arrayList) {
            this.subgroups = arrayList;
        } else {
            this.subgroups = subgroups;
        }
    }

    public ConditionGroupSpec(List<LogicConditionSpec> conditions) {
        this(conditions, new ArrayList<>());
    }

    public ConditionGroupSpec() {
        this(new ArrayList<>(), new ArrayList<>());
    }

    public ConditionSpec newCondition(final LogicOperator logicOperator, final ExpressionSpec expressionSpec) {
        final ConditionSpec conditionSpec = new ConditionSpec();
        conditionSpec.setLhs(expressionSpec);
        final LogicConditionSpec logicConditionSpec = new LogicConditionSpec(logicOperator, conditionSpec);
        conditions.add(logicConditionSpec);
        return conditionSpec;
    }

    public LogicConditionGroupSpec newSubgroup(final LogicOperator logicOperator) {
        final LogicConditionGroupSpec logicConditionGroupSpec = new LogicConditionGroupSpec(logicOperator);
        subgroups.add(logicConditionGroupSpec);
        return logicConditionGroupSpec;
    }

    public ConditionGroup toConditionGroup(final SelectExpressionMapper selectExpressionMapper, final Set<Table> selectedTables) {
        final List<LogicConditionGroup> subConditionGroups = subgroups.stream()
                .map(subgroup -> {
                    final ConditionGroup conditionGroup = subgroup.conditionGroupSpec().toConditionGroup(selectExpressionMapper, selectedTables);
                    return new LogicConditionGroup(subgroup.logicOperator(), conditionGroup);
                })
                .toList();

        return new ConditionGroup(conditions.stream()
                .map(spec -> new LogicCondition(spec.logicOperator(),
                        spec.conditionSpec().toCondition(selectExpressionMapper, selectedTables)))
                .toList(),
                subConditionGroups);
    }
}
