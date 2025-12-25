package org.litebridge.orm.api.dto;

import org.litebridge.orm.api.select.impl.AbstractFromClauseTerminal;
import org.litebridge.orm.api.select.impl.AbstractSelector;

public class DtoFromClauseTerminal<DTO> extends AbstractFromClauseTerminal<DTO,
        DtoJoinClause<DTO>,
        DtoJoinConditionClause<DTO>,
        DtoJoinConditionClauseTerminal<DTO>,
        DtoWhereConditionClause<DTO>,
        DtoWhereConditionClauseTerminal<DTO>> {

    public DtoFromClauseTerminal(final AbstractSelector<DTO> delegate) {
        super(delegate);
    }

    @Override
    public DtoWhereConditionClause<DTO> where(final String field) {
        return new DtoWhereConditionClause<>(selectSpec.newWhereCondition(field), new DtoWhereConditionClauseTerminal<>(delegate));
    }

    @Override
    public DtoJoinClause<DTO> join(final String table) {
        return new DtoJoinClause<>(selectSpec.newJoinSpec(table), delegate);
    }
}
