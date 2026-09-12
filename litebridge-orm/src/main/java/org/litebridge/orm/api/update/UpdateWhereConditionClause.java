package org.litebridge.orm.api.update;

import org.litebridge.orm.api.select.ConditionClause;

/**
 * Condition clause interface for specifying conditions in an update statement WHERE clause.
 *
 * @param <DTO>  the entity/DTO type or row type
 * @param <SELF> the condition clause type itself
 * @param <WCCT> the terminal clause type
 */
public sealed interface UpdateWhereConditionClause<DTO,
        SELF extends UpdateWhereConditionClause<DTO, SELF, WCCT>,
        WCCT extends UpdateWhereConditionClauseTerminal<DTO, SELF, WCCT>>

        extends ConditionClause<DTO, SELF, WCCT> permits DtoUpdateWhereConditionClause, SqlUpdateWhereConditionClause {

}
