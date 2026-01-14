package org.litebridge.orm.api.dto;

import org.litebridge.db.spi.Column;
import org.litebridge.orm.api.select.impl.AbstractJoinClause;
import org.litebridge.orm.api.select.model.JoinSpec;
import org.litebridge.orm.persistence.OrmTable;

public final class DtoJoinClause<DTO> extends AbstractJoinClause<DTO,
        DtoJoinConditionClause<DTO>,
        DtoJoinConditionClauseTerminal<DTO>> {

    private final OrmTable table;

    public DtoJoinClause(final JoinSpec joinSpec, final DtoSelector<DTO> delegate) {
        super(joinSpec, delegate);
        table = delegate.table();
    }

    @Override
    public DtoJoinConditionClause<DTO> on(final String field) {
        final Column column = table.getColumnForFieldName(field);
        final DtoJoinConditionClauseTerminal<DTO> joinConditionClauseTerminal = new DtoJoinConditionClauseTerminal<>(joinSpec, (DtoSelector<DTO>) delegate);
        return new DtoJoinConditionClause<>(joinSpec.newCondition(column), joinConditionClauseTerminal);
    }

    @Override
    public DtoJoinConditionClauseTerminal<DTO> using(final String field) {
        final String column = table.getColumnForFieldName(field).name();
        joinSpec.using(column);
        return new DtoJoinConditionClauseTerminal<>(joinSpec, (DtoSelector<DTO>) delegate);
    }
}
