package org.litebridge.orm.api.update;

import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.api.select.ConditionClauseTerminal;

/**
 * Terminal interface for update WHERE condition clauses.
 *
 * @param <DTO>  the mapped DTO/entity type or row type
 * @param <WCC>  the where condition clause type
 * @param <SELF> the self-referencing terminal type
 */
public sealed interface UpdateWhereConditionClauseTerminal<DTO,
        WCC extends UpdateWhereConditionClause<DTO, WCC, SELF, QCB>,
        SELF extends UpdateWhereConditionClauseTerminal<DTO, WCC, SELF, QCB>,
        QCB extends QueryConditionBuilder<DTO, ?, ?, ?, QCB>>

        extends
        ConditionClauseTerminal<DTO, WCC, SELF, QCB>,
        UpdateQuery

        permits DtoUpdateWhereConditionClauseTerminal, SqlUpdateWhereConditionClauseTerminal {

}
