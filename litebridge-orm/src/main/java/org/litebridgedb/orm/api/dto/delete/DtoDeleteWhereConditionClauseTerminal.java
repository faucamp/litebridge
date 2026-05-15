package org.litebridgedb.orm.api.dto.delete;

import org.litebridgedb.orm.api.delete.DeleteWhereConditionClauseTerminal;

public sealed interface DtoDeleteWhereConditionClauseTerminal<DTO>

        extends
        DeleteWhereConditionClauseTerminal<DTO,
                DtoDeleteWhereConditionClause<DTO>,
                DtoDeleteWhereConditionClauseTerminal<DTO>>

        permits DtoDeleteWhereConditionClauseTerminalImpl {

}
