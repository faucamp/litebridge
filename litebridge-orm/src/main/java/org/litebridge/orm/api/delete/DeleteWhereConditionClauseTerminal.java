package org.litebridge.orm.api.delete;

import org.litebridge.orm.api.select.ConditionClauseTerminal;
import org.litebridge.orm.api.select.OrderByClause;
import org.litebridge.orm.api.select.OrderByClauseChain;

public interface DeleteWhereConditionClauseTerminal<DTO,
        WCC extends DeleteWhereConditionClause<DTO, WCC, SELF>,
        SELF extends DeleteWhereConditionClauseTerminal<DTO, WCC, SELF>>

        extends ConditionClauseTerminal<DTO, WCC, SELF>,
        DeleteTerminal<DTO> {

}
