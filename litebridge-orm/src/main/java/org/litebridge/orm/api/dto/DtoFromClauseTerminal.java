package org.litebridge.orm.api.dto;

import org.litebridge.db.spi.Column;
import org.litebridge.orm.api.select.impl.AbstractFromClauseTerminal;
import org.litebridge.orm.persistence.Table;

import java.util.Arrays;

public final class DtoFromClauseTerminal<DTO> extends AbstractFromClauseTerminal<DTO,
        DtoJoinClause<DTO>,
        DtoJoinConditionClause<DTO>,
        DtoJoinConditionClauseTerminal<DTO>,
        DtoWhereConditionClause<DTO>,
        DtoWhereConditionClauseTerminal<DTO>,
        DtoOrderByClause<DTO>,
        DtoOrderByClauseChain<DTO>> {

    private final Table table;

    public DtoFromClauseTerminal(final DtoSelector<DTO> delegate) {
        super(delegate);
        table = delegate.table();
    }

    @Override
    public DtoWhereConditionClause<DTO> where(final String field) {
        final String column = table.getColumnForFieldName(field).getName();
        return new DtoWhereConditionClause<>(selectSpec.newWhereCondition(column), new DtoWhereConditionClauseTerminal<>((DtoSelector<DTO>) delegate));
    }

    @Override
    public DtoJoinClause<DTO> join(final String table) {
        return new DtoJoinClause<>(selectSpec.newJoinSpec(table), (DtoSelector<DTO>) delegate);
    }

    @Override
    public DtoOrderByClause<DTO> orderBy(final String... fields) {
        final String[] columns = Arrays.stream(fields)
                .map(table::getColumnForFieldName)
                .map(Column::getName)
                .toArray(String[]::new);
        return new DtoOrderByClause<>(selectSpec.newOrderBy(columns), delegate);
    }
}
