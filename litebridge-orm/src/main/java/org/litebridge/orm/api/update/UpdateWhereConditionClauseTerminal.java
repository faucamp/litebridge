package org.litebridge.orm.api.update;

import org.litebridge.orm.api.dto.update.DtoUpdateWhereConditionClauseTerminal;
import org.litebridge.orm.api.select.ConditionClauseTerminal;
import org.litebridge.orm.api.sql.update.SqlUpdateWhereConditionClauseTerminal;

public sealed interface UpdateWhereConditionClauseTerminal<DTO,
        WCC extends UpdateWhereConditionClause<DTO, WCC, SELF>,
        SELF extends UpdateWhereConditionClauseTerminal<DTO, WCC, SELF>>

        extends
        ConditionClauseTerminal<DTO, WCC, SELF>,
        UpdateQuery

        permits DtoUpdateWhereConditionClauseTerminal, SqlUpdateWhereConditionClauseTerminal {

}
