package org.litebridge.orm.api.update;

import org.litebridge.orm.api.select.ConditionClauseTerminal;

/**
 * Terminal interface for update WHERE condition clauses.
 *
 * @param <DTO>  the mapped DTO/entity type or row type
 * @param <WCC>  the where condition clause type
 * @param <SELF> the self-referencing terminal type
 */
public sealed interface UpdateWhereConditionClauseTerminal<DTO,
        WCC extends UpdateWhereConditionClause<DTO, WCC, SELF>,
        SELF extends UpdateWhereConditionClauseTerminal<DTO, WCC, SELF>>

        extends
        ConditionClauseTerminal<DTO, WCC, SELF>,
        UpdateQuery

        permits DtoUpdateWhereConditionClauseTerminal, SqlUpdateWhereConditionClauseTerminal {

}
