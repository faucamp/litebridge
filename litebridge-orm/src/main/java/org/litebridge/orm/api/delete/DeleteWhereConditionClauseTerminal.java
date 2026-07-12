package org.litebridge.orm.api.delete;

import org.litebridge.orm.api.dto.delete.DtoDeleteWhereConditionClauseTerminal;
import org.litebridge.orm.api.select.ConditionClauseTerminal;
import org.litebridge.orm.api.sql.delete.SqlDeleteWhereConditionClauseTerminal;

public sealed interface DeleteWhereConditionClauseTerminal<DTO,
        WCC extends DeleteWhereConditionClause<DTO, WCC, SELF>,
        SELF extends DeleteWhereConditionClauseTerminal<DTO, WCC, SELF>>

        extends
        ConditionClauseTerminal<DTO, WCC, SELF>,
        DeleteQuery

        permits
        DtoDeleteWhereConditionClauseTerminal,
        SqlDeleteWhereConditionClauseTerminal {

}
