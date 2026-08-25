package org.litebridge.orm.api.update;

import org.litebridge.orm.api.select.ConditionClause;

public sealed interface UpdateWhereConditionClause<DTO,
        SELF extends UpdateWhereConditionClause<DTO, SELF, WCCT>,
        WCCT extends UpdateWhereConditionClauseTerminal<DTO, SELF, WCCT>>

        extends ConditionClause<DTO, SELF, WCCT> permits DtoUpdateWhereConditionClause, SqlUpdateWhereConditionClause {

}
