package org.litebridge.orm.api.dto;

import org.litebridge.orm.api.select.impl.AbstractJoinClause;
import org.litebridge.orm.api.select.impl.AbstractSelector;
import org.litebridge.orm.api.select.model.JoinSpec;

public class DtoJoinClause<DTO> extends AbstractJoinClause<DTO,
        DtoJoinConditionClause<DTO>,
        DtoJoinConditionClauseTerminal<DTO>> {

    public DtoJoinClause(final JoinSpec joinSpec, final AbstractSelector<DTO> delegate) {
        super(joinSpec, delegate);
    }

    @Override
    public DtoJoinConditionClause<DTO> on(final String column) {
        final DtoJoinConditionClauseTerminal<DTO> joinConditionClauseTerminal = new DtoJoinConditionClauseTerminal<>(joinSpec, delegate);
        return new DtoJoinConditionClause<>(joinSpec.newCondition(column), joinConditionClauseTerminal);
    }
}
