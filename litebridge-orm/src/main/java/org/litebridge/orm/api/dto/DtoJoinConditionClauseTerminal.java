package org.litebridge.orm.api.dto;

import org.litebridge.db.spi.Column;
import org.litebridge.orm.api.select.impl.AbstractJoinConditionClauseTerminal;
import org.litebridge.orm.api.select.model.JoinSpec;
import org.litebridge.orm.persistence.Table;

import java.util.Arrays;

public final class DtoJoinConditionClauseTerminal<DTO> extends AbstractJoinConditionClauseTerminal<DTO,
        DtoJoinConditionClause<DTO>,
        DtoJoinConditionClauseTerminal<DTO>,
        DtoOrderByClause<DTO>,
        DtoOrderByClauseChain<DTO>> {

    private final Table table;

    public DtoJoinConditionClauseTerminal(final JoinSpec joinSpec, final DtoSelector<DTO> delegate) {
        super(joinSpec, delegate);
        this.table = delegate.table();
    }

    @Override
    public DtoJoinConditionClause<DTO> and(final String field) {
        final String column = table.getColumnForFieldName(field).getName();
        return new DtoJoinConditionClause<>(joinSpec.newCondition(column), this);
    }

    @Override
    public DtoOrderByClause<DTO> orderBy(final String... fields) {
        final String[] columns = Arrays.stream(fields)
                .map(table::getColumnForFieldName)
                .map(Column::getName)
                .toArray(String[]::new);
        return new DtoOrderByClause<>(selectSpec.newOrderBy(columns), (DtoSelector<DTO>) delegate);
    }
}
