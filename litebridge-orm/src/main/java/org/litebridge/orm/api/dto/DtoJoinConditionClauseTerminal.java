package org.litebridge.orm.api.dto;

import org.litebridge.orm.api.select.impl.AbstractJoinConditionClauseTerminal;
import org.litebridge.orm.api.select.impl.AbstractSelector;
import org.litebridge.orm.api.select.model.JoinSpec;

public class DtoJoinConditionClauseTerminal<DTO> extends AbstractJoinConditionClauseTerminal<DTO,
        DtoJoinConditionClause<DTO>,
        DtoJoinConditionClauseTerminal<DTO>> {

    public DtoJoinConditionClauseTerminal(final JoinSpec joinSpec, final AbstractSelector<DTO> delegate) {
        super(joinSpec, delegate);
    }

    @Override
    public DtoJoinConditionClause<DTO> and(final String field) {
        return new DtoJoinConditionClause<>(joinSpec.newCondition(field), this);
    }
}
