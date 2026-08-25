package org.litebridge.orm.api.update;

import org.litebridge.orm.api.select.ConditionClauseTerminal;

public sealed interface UpdateWhereConditionClauseTerminal<DTO,
        WCC extends UpdateWhereConditionClause<DTO, WCC, SELF>,
        SELF extends UpdateWhereConditionClauseTerminal<DTO, WCC, SELF>>

        extends
        ConditionClauseTerminal<DTO, WCC, SELF>,
        UpdateQuery

        permits DtoUpdateWhereConditionClauseTerminal, SqlUpdateWhereConditionClauseTerminal {

}
