package org.litebridge.orm.api.dto;

import org.litebridge.orm.api.select.JoinConditionClause;
import org.litebridge.orm.api.select.impl.ConditionClauseImpl;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.api.select.model.ConditionSpec;

public final class DtoJoinConditionClause<DTO> extends ConditionClauseImpl<DTO,
        DtoJoinConditionClause<DTO>,
        DtoJoinConditionClauseTerminal<DTO>>

        implements JoinConditionClause<DTO, DtoJoinConditionClause<DTO>,
        DtoJoinConditionClauseTerminal<DTO>> {

    public DtoJoinConditionClause(final ConditionSpec condition, final DtoJoinConditionClauseTerminal<DTO> conditionTerminal, final LitebridgeContext litebridgeContext) {
        super(condition, conditionTerminal, litebridgeContext);
    }
}
