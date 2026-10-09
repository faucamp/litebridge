package org.litebridge.orm.api.condition;

import org.litebridge.orm.api.select.ConditionClauseTerminal;

public interface DtoConditionClauseTerminal<DTO,
        CC extends DtoConditionClause<DTO, CC, SELF, QCB>,
        SELF extends DtoConditionClauseTerminal<DTO, CC, SELF, QCB>,
        QCB extends QueryConditionBuilder<DTO, ?, ?, ?, QCB>>

        extends ConditionClauseTerminal<DTO, CC, SELF, QCB> {
}
