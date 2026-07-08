package org.litebridgedb.db.spi.query;

import java.util.Collections;
import java.util.List;

/**
 * Groups conditions and condition subgroups using a logical operator.
 *
 * @param logicOperator   The logical operator used to combine conditions and sub-groups.
 * @param conditions      A list of conditions to be combined.
 * @param conditionGroups A list of subgroups of conditions to be combined.
 */
public record ConditionGroup(LogicOperator logicOperator,
                             List<Condition> conditions,
                             List<ConditionGroup> conditionGroups) {

    public ConditionGroup(LogicOperator logicOperator,
                          List<Condition> conditions) {
        this(logicOperator, conditions, Collections.emptyList());
    }
}
