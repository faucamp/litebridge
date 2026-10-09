package org.litebridge.orm.api.update;

import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.api.select.ConditionClause;

/**
 * Condition clause interface for specifying conditions in an update statement WHERE clause.
 *
 * @param <DTO>  the entity/DTO type or row type
 * @param <SELF> the condition clause type itself
 * @param <WCCT> the terminal clause type
 */
public sealed interface UpdateWhereConditionClause<DTO,
        SELF extends UpdateWhereConditionClause<DTO, SELF, WCCT, QCB>,
        WCCT extends UpdateWhereConditionClauseTerminal<DTO, SELF, WCCT, QCB>,
        QCB extends QueryConditionBuilder<DTO, ?, ?, ?, QCB>>

        extends ConditionClause<DTO, SELF, WCCT, QCB> permits DtoUpdateWhereConditionClause, SqlUpdateWhereConditionClause {

}
