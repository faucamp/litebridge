package org.litebridge.orm.api.dto.delete;

import org.litebridge.orm.api.delete.DeleteWhereConditionClauseTerminal;

public sealed interface DtoDeleteWhereConditionClauseTerminal<DTO>

        extends
        DeleteWhereConditionClauseTerminal<DTO,
                DtoDeleteWhereConditionClause<DTO>,
                DtoDeleteWhereConditionClauseTerminal<DTO>>

        permits DtoDeleteWhereConditionClauseTerminalImpl {

}
