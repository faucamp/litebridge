package org.litebridge.orm.api.dto;

import org.litebridge.orm.api.select.WhereConditionClauseTerminal;
import org.litebridge.orm.api.select.impl.AbstractSelector;
import org.litebridge.orm.api.select.impl.WhereClauseTerminalImpl;

public class DtoWhereConditionClauseTerminal<DTO> extends WhereClauseTerminalImpl<DTO>
        implements WhereConditionClauseTerminal<DTO, DtoWhereConditionClause<DTO>, DtoWhereConditionClauseTerminal<DTO>> {

    public DtoWhereConditionClauseTerminal(final AbstractSelector<DTO> delegate) {
        super(delegate);
    }

    @Override
    public DtoWhereConditionClause<DTO> and(final String field) {
        return new DtoWhereConditionClause<>(selectSpec.newWhereCondition(field), this);
    }
}
