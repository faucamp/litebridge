package org.litebridge.orm.api.delete;

import org.litebridge.orm.api.select.ConditionClause;

public interface DeleteWhereConditionClause<DTO,
        SELF extends DeleteWhereConditionClause<DTO, SELF, WCCT>,
        WCCT extends DeleteWhereConditionClauseTerminal<DTO, SELF, WCCT>>

        extends ConditionClause<DTO, SELF, WCCT> {

}
