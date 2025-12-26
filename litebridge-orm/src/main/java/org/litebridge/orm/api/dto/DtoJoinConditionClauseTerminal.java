package org.litebridge.orm.api.dto;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.orm.api.select.JoinClauseTerminal;
import org.litebridge.orm.api.select.impl.AbstractJoinConditionClauseTerminal;
import org.litebridge.orm.api.select.model.JoinSpec;
import org.litebridge.orm.persistence.Table;

import java.util.Arrays;

public final class DtoJoinConditionClauseTerminal<DTO>
        extends AbstractJoinConditionClauseTerminal<DTO,
        DtoJoinConditionClause<DTO>,
        DtoJoinConditionClauseTerminal<DTO>,
        DtoOrderByClause<DTO>,
        DtoOrderByClauseChain<DTO>>

        implements JoinClauseTerminal<DTO,
        DtoJoinClause<DTO>,
        DtoJoinConditionClause<DTO>,
        DtoJoinConditionClauseTerminal<DTO>,
        DtoWhereConditionClause<DTO>,
        DtoWhereConditionClauseTerminal<DTO>,
        DtoOrderByClause<DTO>,
        DtoOrderByClauseChain<DTO>>,

        DtoJoinClassTerminal<DTO> {

    private final Table table;

    public DtoJoinConditionClauseTerminal(final JoinSpec joinSpec, final DtoSelector<DTO> delegate) {
        super(joinSpec, delegate);
        this.table = delegate.table();
    }

    @Override
    public DtoJoinConditionClause<DTO> and(final String field) {
        final Column column = table.getColumnForFieldName(field);
        return new DtoJoinConditionClause<>(joinSpec.newCondition(column), this);
    }

    @Override
    public DtoWhereConditionClause<DTO> where(final String field) {
        final Column column = table.getColumnForFieldName(field);
        return new DtoWhereConditionClause<>(selectSpec.newWhereCondition(column), new DtoWhereConditionClauseTerminal<>((DtoSelector<DTO>) delegate));
    }

    @Override
    public DtoJoinClause<DTO> join(final Class<?> dtoClass) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DtoOrderByClause<DTO> orderBy(final String... fields) {
        final String[] columns = Arrays.stream(fields)
                .map(table::getColumnForFieldName)
                .map(ColumnMetaData::name)
                .toArray(String[]::new);
        return new DtoOrderByClause<>(selectSpec.newOrderBy(columns), (DtoSelector<DTO>) delegate);
    }
}
