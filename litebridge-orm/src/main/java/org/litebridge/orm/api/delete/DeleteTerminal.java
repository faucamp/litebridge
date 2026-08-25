package org.litebridge.orm.api.delete;

/**
 * Represents the final stage of a delete query that can be executed.
 */
public sealed interface DeleteTerminal permits DeleteWhereConditionClauseTerminal, DtoDeleteStart, DtoDeleteWhereConditionClauseTerminalImpl, SqlDeleteStart, SqlDeleteWhereConditionClauseTerminalImpl {

}
