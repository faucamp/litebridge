package org.litebridgedb.orm.api.delete;

import org.litebridgedb.orm.api.dto.delete.DtoDeleteWhereConditionClauseTerminal;
import org.litebridgedb.orm.api.select.ConditionClauseTerminal;
import org.litebridgedb.orm.api.sql.delete.SqlDeleteWhereConditionClauseTerminal;

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
