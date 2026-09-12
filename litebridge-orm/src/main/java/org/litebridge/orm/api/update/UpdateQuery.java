package org.litebridge.orm.api.update;

/**
 * Marker interface representing an update query specification or step.
 */
public sealed interface UpdateQuery
        permits UpdateStep, UpdateWhereConditionClauseTerminal {
}
