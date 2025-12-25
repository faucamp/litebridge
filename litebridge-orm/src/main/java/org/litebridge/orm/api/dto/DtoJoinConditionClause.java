package org.litebridge.orm.api.dto;

import org.litebridge.orm.api.select.JoinConditionClause;
import org.litebridge.orm.api.select.impl.ConditionClauseImpl;
import org.litebridge.orm.api.select.model.ConditionSpec;

public class DtoJoinConditionClause<DTO> extends ConditionClauseImpl<DTO,
        DtoJoinConditionClause<DTO>,
        DtoJoinConditionClauseTerminal<DTO>>

        implements JoinConditionClause<DTO, DtoJoinConditionClause<DTO>,
        DtoJoinConditionClauseTerminal<DTO>> {

    public DtoJoinConditionClause(final ConditionSpec condition, final DtoJoinConditionClauseTerminal<DTO> conditionTerminal) {
        super(condition, conditionTerminal);
    }
}
