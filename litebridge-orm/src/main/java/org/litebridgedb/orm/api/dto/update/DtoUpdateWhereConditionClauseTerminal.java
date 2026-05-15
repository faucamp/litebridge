package org.litebridgedb.orm.api.dto.update;

import org.litebridgedb.orm.api.update.UpdateWhereConditionClauseTerminal;

public sealed interface DtoUpdateWhereConditionClauseTerminal<DTO>

        extends
        UpdateWhereConditionClauseTerminal<DTO,
                        DtoUpdateWhereConditionClause<DTO>,
                        DtoUpdateWhereConditionClauseTerminal<DTO>>

        permits DtoUpdateWhereConditionClauseTerminalImpl {

}
