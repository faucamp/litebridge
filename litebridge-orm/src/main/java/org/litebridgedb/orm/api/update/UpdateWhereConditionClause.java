package org.litebridgedb.orm.api.update;

import org.litebridgedb.orm.api.select.ConditionClause;

public interface UpdateWhereConditionClause<DTO,
        SELF extends UpdateWhereConditionClause<DTO, SELF, WCCT>,
        WCCT extends UpdateWhereConditionClauseTerminal<DTO, SELF, WCCT>>

        extends ConditionClause<DTO, SELF, WCCT> {

}
