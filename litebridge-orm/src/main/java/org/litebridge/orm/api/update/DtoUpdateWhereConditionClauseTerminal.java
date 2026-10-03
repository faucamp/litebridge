package org.litebridge.orm.api.update;

/**
 * The terminal interface for DTO update where condition clauses.
 *
 * @param <DTO> the DTO type
 */
public sealed interface DtoUpdateWhereConditionClauseTerminal<DTO>

        extends
        UpdateWhereConditionClauseTerminal<DTO,
                        DtoUpdateWhereConditionClause<DTO>,
                        DtoUpdateWhereConditionClauseTerminal<DTO>>

        permits DtoUpdateWhereConditionClauseTerminalImpl {

}
