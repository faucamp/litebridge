package org.litebridge.orm.api.dto;

import org.litebridge.orm.api.select.impl.AbstractJoinClause;
import org.litebridge.orm.api.select.model.JoinSpec;
import org.litebridge.orm.persistence.Table;

public final class DtoJoinClause<DTO> extends AbstractJoinClause<DTO,
        DtoJoinConditionClause<DTO>,
        DtoJoinConditionClauseTerminal<DTO>> {

    private final Table table;

    public DtoJoinClause(final JoinSpec joinSpec, final DtoSelector<DTO> delegate) {
        super(joinSpec, delegate);
        table = delegate.table();
    }

    @Override
    public DtoJoinConditionClause<DTO> on(final String field) {
        final String column = table.getColumnForFieldName(field).getName();
        final DtoJoinConditionClauseTerminal<DTO> joinConditionClauseTerminal = new DtoJoinConditionClauseTerminal<>(joinSpec, (DtoSelector<DTO>) delegate);
        return new DtoJoinConditionClause<>(joinSpec.newCondition(column), joinConditionClauseTerminal);
    }
}
