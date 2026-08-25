package org.litebridge.orm.api.update;

public sealed interface UpdateQuery
        permits UpdateStep, UpdateWhereConditionClauseTerminal {
}
