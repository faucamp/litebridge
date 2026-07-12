package org.litebridge.orm.api.dto.update;

import org.litebridge.orm.api.update.UpdateWhereConditionClauseTerminal;

public sealed interface DtoUpdateWhereConditionClauseTerminal<DTO>

        extends
        UpdateWhereConditionClauseTerminal<DTO,
                        DtoUpdateWhereConditionClause<DTO>,
                        DtoUpdateWhereConditionClauseTerminal<DTO>>

        permits DtoUpdateWhereConditionClauseTerminalImpl {

}
