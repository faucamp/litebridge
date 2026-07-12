package org.litebridge.orm.api.delete;

import org.litebridge.orm.api.dto.delete.DtoDeleteWhereConditionClauseTerminal;
import org.litebridge.orm.api.select.ConditionClauseTerminal;
import org.litebridge.orm.api.sql.delete.SqlDeleteWhereConditionClauseTerminal;

/**
 * Interface for the terminal part of a WHERE condition clause in a delete query.
 *
 * @param <DTO>  the type of the DTO
 * @param <WCC>  the type of the condition clause
 * @param <SELF> the type of the terminal condition clause itself
 */
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
