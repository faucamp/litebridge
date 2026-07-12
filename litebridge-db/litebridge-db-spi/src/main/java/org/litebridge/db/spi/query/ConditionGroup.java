package org.litebridge.db.spi.query;

import java.util.Collections;
import java.util.List;

/**
 * A group of logically combined conditions and condition subgroups.
 *
 * @param conditions A list of conditions to be combined.
 * @param subgroups  A list of subgroups of conditions to be combined.
 */
public record ConditionGroup(List<LogicCondition> conditions,
                             List<LogicConditionGroup> subgroups) {

    public ConditionGroup(List<LogicCondition> conditions) {
        this(conditions, Collections.emptyList());
    }

    public ConditionGroup(LogicCondition condition) {
        this(List.of(condition));
    }

    public boolean isEmpty() {
        return conditions.isEmpty() && subgroups.isEmpty();
    }
}
