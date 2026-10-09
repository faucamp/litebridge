package org.litebridge.orm.api.delete;

import org.litebridge.orm.api.condition.QueryConditionBuilder;
import org.litebridge.orm.api.select.ConditionClause;

/**
 * Interface for a WHERE condition clause in a delete query.
 *
 * @param <DTO>  the type of the DTO
 * @param <SELF> the type of the clause itself (for fluent API)
 * @param <WCCT> the type of the terminal condition clause
 */
public sealed interface DeleteWhereConditionClause<DTO,
        SELF extends DeleteWhereConditionClause<DTO, SELF, WCCT, QCB>,
        WCCT extends DeleteWhereConditionClauseTerminal<DTO, SELF, WCCT, QCB>,
        QCB extends QueryConditionBuilder<DTO, ?, ?, ?, QCB>>

        extends ConditionClause<DTO, SELF, WCCT, QCB>

        permits
        DtoDeleteWhereConditionClause,
        SqlDeleteWhereConditionClause {

}
