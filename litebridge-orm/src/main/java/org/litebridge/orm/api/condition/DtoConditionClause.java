package org.litebridge.orm.api.condition;

import org.litebridge.orm.api.select.ConditionClause;

public interface DtoConditionClause<DTO,
        SELF extends DtoConditionClause<DTO, SELF, CCT, QCB>,
        CCT extends DtoConditionClauseTerminal<DTO, SELF, CCT, QCB>,
        QCB extends QueryConditionBuilder<DTO, ?, ?, ?, QCB>>

        extends ConditionClause<DTO, SELF, CCT, QCB> {
}
