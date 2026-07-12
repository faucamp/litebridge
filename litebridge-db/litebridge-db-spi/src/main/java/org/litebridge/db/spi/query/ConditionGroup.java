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

    /**
     * Constructs a {@code ConditionGroup} with the specified conditions.
     *
     * @param conditions the list of conditions
     */
    public ConditionGroup(final List<LogicCondition> conditions) {
        this(conditions, Collections.emptyList());
    }

    /**
     * Constructs a {@code ConditionGroup} with a single condition.
     *
     * @param condition the condition to add
     */
    public ConditionGroup(final LogicCondition condition) {
        this(List.of(condition));
    }

    /**
     * Checks if the condition group is empty.
     *
     * @return {@code true} if the group contains no conditions or subgroups; {@code false} otherwise
     */
    public boolean isEmpty() {
        return conditions.isEmpty() && subgroups.isEmpty();
    }
}
