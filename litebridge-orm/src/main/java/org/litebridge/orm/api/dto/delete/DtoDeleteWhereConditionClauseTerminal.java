package org.litebridge.orm.api.dto.delete;

import org.litebridge.orm.api.delete.DeleteWhereConditionClauseTerminal;

/**
 * Terminal clause for DTO delete WHERE conditions.
 *
 * @param <DTO> the type of the DTO
 */
public sealed interface DtoDeleteWhereConditionClauseTerminal<DTO>

        extends
        DeleteWhereConditionClauseTerminal<DTO,
                DtoDeleteWhereConditionClause<DTO>,
                DtoDeleteWhereConditionClauseTerminal<DTO>>

        permits DtoDeleteWhereConditionClauseTerminalImpl {

}
